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
class AgoraVideoRtcManager @Inject constructor(
    @ApplicationContext private val context: Context
) : RtcManager {

    private val _events = MutableSharedFlow<RtcEvent>(extraBufferCapacity = 8)
    override val events: Flow<RtcEvent> = _events.asSharedFlow()

    private var rtcEngine: RtcEngine? = null

    private fun ensureEngine() {
        if (rtcEngine != null) return

        val rtcListener = object : IRtcEngineEventHandler() {

            override fun onJoinChannelSuccess(
                channel: String?,
                uid: Int,
                elapsed: Int
            ) {
                _events.tryEmit(RtcEvent.Connected)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                _events.tryEmit(RtcEvent.RemoteJoined(uid))
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                _events.tryEmit(RtcEvent.RemoteLeft(uid))
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                _events.tryEmit(RtcEvent.CallEnded)
            }

            override fun onError(err: Int) {
                _events.tryEmit(RtcEvent.Error(err))
            }
        }

        Log.d(
            "RTM",
            "Creating RTC Engine | appId=${BuildConfig.AGORA_APP_ID.take(8)}..."
        )

        rtcEngine = RtcEngine.create(
            context.applicationContext,
            BuildConfig.AGORA_APP_ID,
            rtcListener
        ).apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            enableAudio()
            enableVideo()
            startPreview()
        }
    }

    override fun join(callId: String, channel: String, token: String?) {
        ensureEngine()
        rtcEngine?.joinChannel(
            token,     // ✅ RTC token from backend
            channel,   // ✅ channel from backend
            null,
            0
        )
    }

    override fun leave() {
        rtcEngine?.apply {
            stopPreview()
            leaveChannel()
        }

        // 🔥 notify UI to close call screen
        _events.tryEmit(RtcEvent.CallEnded)
    }

    /** Call ONLY on logout / app shutdown */
    fun destroy() {
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
    }
}