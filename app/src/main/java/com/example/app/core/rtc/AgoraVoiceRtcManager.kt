package com.example.app.core.rtc

import android.content.Context
import android.util.Log
import com.example.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgoraVoiceRtcManager @Inject constructor(
    @ApplicationContext private val context: Context
) : RtcManager {
    private val _events = MutableSharedFlow<RtcEvent>(extraBufferCapacity = 8)
    override val events: Flow<RtcEvent> = _events.asSharedFlow()
    private var activeCallId: String? = null
    private var rtcEngine: RtcEngine? = null
    private fun ensureEngine(): Boolean {
        if (rtcEngine != null) return true
        val rtcListener = object : IRtcEngineEventHandler() {

            override fun onJoinChannelSuccess(
                channel: String?,
                uid: Int,
                elapsed: Int
            ) {
                Log.d("RTM", "onJoinChannelSuccess")
                _events.tryEmit(RtcEvent.Connected)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d("RTM", "onUserJoined")
                _events.tryEmit(RtcEvent.RemoteJoined(uid))
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                Log.d("RTM", "onUserOffline")
                _events.tryEmit(RtcEvent.RemoteLeft(uid))
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                Log.d("RTM", "onLeaveChannel")
                if (activeCallId != null) {
                    _events.tryEmit(RtcEvent.CallEnded)
                }
            }

            override fun onError(err: Int) {
                Log.e("RTM", "Agora RTC onError code=$err")
                _events.tryEmit(RtcEvent.Error(err))
            }
        }
        val engine = try {
            Log.d(
                "RTM",
                "Creating RTC Engine | appId=${BuildConfig.AGORA_APP_ID.take(8)}..."
            )
            RtcEngine.create(
                context.applicationContext,
                BuildConfig.AGORA_APP_ID,
                rtcListener
            )
        } catch (e: Exception) {
            Log.e("RTM", "Creating RTC Engine Failed")
            null
        }

        // 🔥 ENGINE CREATION FAILED
        if (engine == null) {
            Log.e("RTM", "Creating RTC Engine Failed2")
            _events.tryEmit(
                RtcEvent.Error(
                    code = -1000 // custom code: engine init failed
                )
            )
            return false
        }
        rtcEngine = engine
        rtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            enableAudio()
            disableVideo() // voice-only billing safety
            enableLocalAudio(true)
            muteLocalAudioStream(false)
        }
        return true
    }

    override fun join(callId: String, channel: String, token: String?) {
        if (activeCallId == callId) {
            Log.w("RTM", "Already joined callId=$callId channel=$channel, ignoring join")
            return
        }

        Log.d("RTM", "join Voice_RTCMANAGER =$channel token=$token")

        val ready = ensureEngine()
        if (!ready) return

        activeCallId = callId
        rtcEngine?.joinChannel(
            token,
            channel,
            null,
            0
        )
    }

    override fun leave() {
        activeCallId = null
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
        // DO NOT emit CallEnded here
        // _events.tryEmit(RtcEvent.CallEnded)
    }

    override fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    override fun enableSpeaker(enable: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enable)
    }


    /*
    When remote user join then start billing.
    onJoinChannelSuccess
    onUserJoined
    onUserOffline
    onConnectionStateChanged
    onLeaveChannel
    onError

    Long calls can exceed token TTL, Refresh token silently, Prevent sudden call drop
    onTokenPrivilegeWillExpire(String token)


    call backend to renewToken
    onRequestToken

    To show audio animation:
    onAudioVolumeIndication

    Show Poor network warning in UI
    onNetworkQuality(int uid, int txQuality, int rxQuality)

    * */

    // on user offline leave rtc manager
    //    fun onUserOffline(uid: Int, reason: Int) {
    //        if (isOneToOneCall) {
    //            rtcManager.leave()
    //        }
    //    }

}