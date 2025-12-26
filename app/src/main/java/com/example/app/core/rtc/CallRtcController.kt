package com.example.app.core.rtc

import android.util.Log
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallStore
import com.example.app.core.di.ApplicationScope
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
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
    @ApplicationScope private val scope: CoroutineScope
) {
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
            Log.d("RTC", "Waiting for RTC credentials…")
            return
        }

        if (rtcJoinStarted) {
            Log.w("RTC", "Join already started, ignoring")
            return
        }

        rtcJoinStarted = true

        Log.d("RTC", "Joining channel=$channel")

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

    // ------------------------------------------------------------
    // RTC EVENTS HANDLING
    // ------------------------------------------------------------

    private fun collectRtcEvents(manager: RtcManager) {
        scope.launch {
            manager.events.collect { event ->
                when (event) {
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

                    else -> {}
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
            Log.w("RTC", "Connect timeout")
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
        Log.d("RTC", "Cleanup called")

        rtcJoinStarted = false
        callStarted = false

        cancelConnectTimeout()

        rtcManager?.leave()
        rtcManager = null

        _videoRenderer.value = null   // 🔥 important
    }

}
