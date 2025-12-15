package com.example.app.core.rtc

import android.content.Context
import com.example.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.Constants
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

    private var rtcEngine: RtcEngine? = null

    private fun ensureEngine() {
        if (rtcEngine != null) return

        val listener = AgoraRtcListener { event ->
            _events.tryEmit(event)
        }

        rtcEngine = RtcEngine.create(
            context.applicationContext,
            BuildConfig.AGORA_APP_ID,
            listener
        ).apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            enableAudio()
            disableVideo() // billing safety
        }
    }

    override fun join(channel: String, uid: Int) {
        ensureEngine()
        rtcEngine?.joinChannel(null, channel, null, uid)
    }

    override fun leave() {
        rtcEngine?.leaveChannel()

        // 🔥 tell UI to close call screen
        _events.tryEmit(RtcEvent.CallEnded)
    }

    /** Call ONLY on logout / app termination */
    fun destroy() {
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
    }
}


//@Singleton
//class AgoraVoiceRtcManager @Inject constructor(
//    @ApplicationContext private val context: Context
//) : RtcManager {
//
//    private val _events = MutableSharedFlow<RtcEvent>(extraBufferCapacity = 8)
//    override val events: Flow<RtcEvent> = _events.asSharedFlow()
//
//    private val rtcEngine: RtcEngine by lazy {
//        val listener = AgoraRtcListener { event ->
//            _events.tryEmit(event)
//        }
//
//        RtcEngine.create(
//            context,
//            BuildConfig.AGORA_APP_ID,
//            listener
//        ).apply {
//            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
//            enableAudio()
//            disableVideo()   // 🔥 billing safety
//        }
//    }
//
//    override fun join(channel: String, uid: Int) {
//        rtcEngine.joinChannel(
//            null,
//            channel,
//            null,
//            uid
//        )
//    }
//
//    override fun leave() {
//        rtcEngine.leaveChannel()
//        RtcEngine.destroy()
//    }
//}

