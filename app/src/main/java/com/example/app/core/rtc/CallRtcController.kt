package com.example.app.core.rtc

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallForegroundService
import com.example.app.core.call.CallStore
import com.example.app.core.call.CallType
import com.example.app.core.di.ApplicationScope
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRtcController @Inject constructor(
    private val rtcManagerFactory: RtcManagerFactory,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _videoRenderer = MutableStateFlow<VideoRenderer?>(null)
    val videoRenderer: StateFlow<VideoRenderer?> = _videoRenderer

    private var rtcManager: RtcManager? = null
    private var rtcJoinStarted = false
    private var callStarted = false

    private var connectTimeoutJob: Job? = null

    init {
        scope.launch {
            CallStore.call.collect { call ->
                val status = call?.status ?: run {
                    cleanup()
                    return@collect
                }

                when (status) {
                    // 🔄 When call transitions to CONNECTING —> join RTC
                    CallStatus.CONNECTING -> joinIfReady(call)

                    // 🟢 When media connects
                    CallStatus.CONNECTED -> {
                        if (callStarted) return@collect
                        callStarted = true
                        cancelConnectTimeout()
                        startCallService()
                    }

                    // ❌ Ended in any way
                    CallStatus.CANCELLED,
                    CallStatus.REJECTED,
                    CallStatus.ENDED -> cleanup()

                    else -> Unit
                }
            }
        }
    }

    // ------------------------------------------------------------
    // RTC JOIN
    // ------------------------------------------------------------

    private fun joinIfReady(call: CallModel) {
        val channel = call.channel
        val token = call.rtcToken

        // wait until RTM delivers both
        if (channel.isNullOrBlank() || token.isNullOrBlank()) {
            Log.d("RTM", "Waiting for RTC credentials…")
            return
        }

        if (rtcJoinStarted) {
            Log.w("RTM", "Join already started, ignoring")
            return
        }

        // Safety check: Verify microphone permission before joining
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            Log.e("RTM", "Cannot join channel: RECORD_AUDIO permission not granted")
            // For incoming calls, UI will handle permission request
            // For outgoing calls, this shouldn't happen as we check before calling
            // Only end call if it's not an incoming call (to avoid silent failure)
            val currentCall = CallStore.current()
            if (currentCall != null && currentCall.status != CallStatus.INCOMING_RINGING) {
                scope.launch {
                    CallEventBus.emit(CallEvent.Ended(call.callId))
                }
            }
            return
        }

        rtcJoinStarted = true

        Log.d("RTM", "Joining channel=$channel")

        rtcManager = rtcManagerFactory.create(call.callType).also { manager ->

            collectRtcEvents(manager)

            // expose renderer only for video-capable managers
            if (manager is AgoraVideoRtcManager) {
                _videoRenderer.value = AgoraVideoRenderer(manager)
            }

            manager.join(call.callId, channel, token)
        }

        startConnectTimeout()
    }

    fun setMuted(muted: Boolean) {
        rtcManager?.muteLocalAudio(muted)
    }

    fun setSpeaker(enabled: Boolean) {

        rtcManager?.enableSpeaker(enabled)

        if (enabled) {
            // ---------- ROUTE TO SPEAKER ----------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val speaker = audioManager.availableCommunicationDevices
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

                speaker?.let { audioManager.setCommunicationDevice(it) }
            } else {
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isSpeakerphoneOn = true
            }

            // ---------- USE MEDIA STREAM (LOUD) ----------
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                max,
                AudioManager.FLAG_PLAY_SOUND
            )
        } else {

            // ---------- BACK TO EARPIECE ----------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                audioManager.isSpeakerphoneOn = false
                audioManager.mode = AudioManager.MODE_NORMAL
            }

            // optional: drop voice call volume to normal mid level
            val mid = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 2
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                mid,
                0
            )
        }
    }

    fun switchCamera() {
        _videoRenderer.value?.switchCamera()
    }

    /*
    fun setSpeaker(enabled: Boolean) {
    rtcManager?.enableSpeaker(enabled)

    // Always treat as a call
    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

    if (enabled) {
        // route to loud speaker
        audioManager.isSpeakerphoneOn = true

        // BOOST voice-call volume instead of using STREAM_MUSIC
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            max,
            AudioManager.FLAG_PLAY_SOUND
        )

    } else {
        // back to earpiece / bluetooth / wired
        audioManager.isSpeakerphoneOn = false

        val mid = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 2
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mid, 0)
    }
}

    */

    // ------------------------------------------------------------
    // RTC EVENTS HANDLING
    // ------------------------------------------------------------

    private fun collectRtcEvents(manager: RtcManager) {
        scope.launch {
            manager.events.collect { event ->
                when (event) {
                    // ----- video specific -----
                    is RtcEvent.RemoteJoined -> {
                        Log.d("RTM", "Remote joined uid=${event.uid}")
                        _remoteUid.value = event.uid
                    }

                    is RtcEvent.RemoteLeft -> {
                        Log.d("RTM", "Remote left")
                        _remoteUid.value = null
                    }

                    // ----- lifecycle -----
                    is RtcEvent.Connected -> {
                        val call = CallStore.current() ?: return@collect
                        CallEventBus.emit(CallEvent.Connected(call.callId))
                    }

                    is RtcEvent.CallEnded -> {
                        val call = CallStore.current() ?: return@collect
                        CallEventBus.emit(CallEvent.Ended(call.callId))
                    }

                    is RtcEvent.Disconnected -> {
                        val call = CallStore.current() ?: return@collect
                        CallEventBus.emit(CallEvent.Ended(call.callId))
                    }

                    is RtcEvent.Error -> {
                        val call = CallStore.current() ?: return@collect
                        CallEventBus.emit(CallEvent.Ended(call.callId))
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // TIMEOUTS
    // ------------------------------------------------------------

    private fun startConnectTimeout() {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = scope.launch {
            delay(15_000)   // 15s
            Log.w("RTM", "Connect timeout")
            val call = CallStore.current() ?: return@launch
            CallEventBus.emit(CallEvent.Ended(call.callId))
        }
    }

    private fun cancelConnectTimeout() {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = null
    }

    // ------------------------------------------------------------
    // CLEANUP
    // ------------------------------------------------------------

    private fun cleanup() {
        Log.d("RTM", "Cleanup called")

        stopCallService()

        rtcJoinStarted = false
        callStarted = false

        cancelConnectTimeout()

        rtcManager?.leave()
        rtcManager = null

        _videoRenderer.value = null
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    // ------------------------------------------------------------
    // FOREGROUND SERVICE HELPER FUNCTION
    // ------------------------------------------------------------

    private fun startCallService() {
        val call = CallStore.current() ?: return
        
        val intent = Intent(context, CallForegroundService::class.java).apply {
            putExtra(
                CallForegroundService.EXTRA_CALL_TYPE,
                if (call.callType == CallType.VIDEO) {
                    CallForegroundService.CALL_TYPE_VIDEO
                } else {
                    CallForegroundService.CALL_TYPE_VOICE
                }
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }


    private fun stopCallService() {
        val intent = Intent(context, com.example.app.core.call.CallForegroundService::class.java)
        context.stopService(intent)
    }

}
