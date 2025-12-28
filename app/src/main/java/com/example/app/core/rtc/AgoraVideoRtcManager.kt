package com.example.app.core.rtc

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.example.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
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
                Log.d("RTM", "Video onJoinChannelSuccess")
                _events.tryEmit(RtcEvent.Connected)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d("RTM", "Video onUserJoined uid=$uid")
                _events.tryEmit(RtcEvent.RemoteJoined(uid))
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                _events.tryEmit(RtcEvent.RemoteLeft(uid))
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                _events.tryEmit(RtcEvent.CallEnded)
            }

            override fun onError(err: Int) {
                Log.e("RTM", "Agora Video RTC error=$err")
                _events.tryEmit(RtcEvent.Error(err))
            }
        }

        val engine = try {
            Log.d(
                "RTM",
                "Creating VIDEO RTC Engine | appId=${BuildConfig.AGORA_APP_ID.take(8)}..."
            )
            RtcEngine.create(
                context.applicationContext,
                BuildConfig.AGORA_APP_ID,
                rtcListener
            )
        } catch (e: Exception) {
            Log.e("RTM", "Creating Video RTC Engine Failed", e)
            null
        }

        if (engine == null) {
            _events.tryEmit(RtcEvent.Error(-1000))
            return false
        }

        rtcEngine = engine

        rtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)

            // 🔊 Audio (same as voice)
            enableAudio()
            enableLocalAudio(true)
            muteLocalAudioStream(false)

            // 🎥 Video
            enableVideo()
            setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                )
            )
        }

        return true
    }

    override fun join(callId: String, channel: String, token: String?) {
        if (activeCallId == callId) {
            Log.w("RTM", "Already joined video callId=$callId")
            return
        }

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

        rtcEngine?.apply {
            stopPreview()
            leaveChannel()
        }

        RtcEngine.destroy()
        rtcEngine = null
    }

    override fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    override fun enableSpeaker(enable: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enable)
    }


    /** Called by UI when local surface is ready */
    fun setupLocalVideo(view: SurfaceView) {
        rtcEngine?.apply {
            val canvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            setupLocalVideo(canvas)
            startPreview()
        }
    }

    /** Called when remote user joins */
    fun setupRemoteVideo(uid: Int, view: SurfaceView) {
        rtcEngine?.setupRemoteVideo(
            VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        )
    }
}

//@Singleton
//class AgoraVideoRtcManager @Inject constructor(
//    @ApplicationContext private val context: Context
//) : RtcManager {
//
//    private val _events = MutableSharedFlow<RtcEvent>(extraBufferCapacity = 8)
//    override val events: Flow<RtcEvent> = _events.asSharedFlow()
//
//    private var rtcEngine: RtcEngine? = null
//
//    private fun ensureEngine() {
//        if (rtcEngine != null) return
//
//        val rtcListener = object : IRtcEngineEventHandler() {
//
//            override fun onJoinChannelSuccess(
//                channel: String?,
//                uid: Int,
//                elapsed: Int
//            ) {
//                _events.tryEmit(RtcEvent.Connected)
//            }
//
//            override fun onUserJoined(uid: Int, elapsed: Int) {
//                _events.tryEmit(RtcEvent.RemoteJoined(uid))
//            }
//
//            override fun onUserOffline(uid: Int, reason: Int) {
//                _events.tryEmit(RtcEvent.RemoteLeft(uid))
//            }
//
//            override fun onLeaveChannel(stats: RtcStats?) {
//                _events.tryEmit(RtcEvent.CallEnded)
//            }
//
//            override fun onError(err: Int) {
//                _events.tryEmit(RtcEvent.Error(err))
//            }
//        }
//
//        Log.d(
//            "RTM",
//            "Creating RTC Engine | appId=${BuildConfig.AGORA_APP_ID.take(8)}..."
//        )
//
//        rtcEngine = RtcEngine.create(
//            context.applicationContext,
//            BuildConfig.AGORA_APP_ID,
//            rtcListener
//        ).apply {
//            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
//            enableAudio()
//            enableVideo()
//            startPreview()
//        }
//    }
//
//    override fun join(callId: String, channel: String, token: String?) {
//        ensureEngine()
//        rtcEngine?.joinChannel(
//            token,     // ✅ RTC token from backend
//            channel,   // ✅ channel from backend
//            null,
//            0
//        )
//    }
//
//    override fun leave() {
//        rtcEngine?.apply {
//            stopPreview()
//            leaveChannel()
//        }
//
//        // 🔥 notify UI to close call screen
//        _events.tryEmit(RtcEvent.CallEnded)
//    }
//
//    /** Call ONLY on logout / app shutdown */
//    fun destroy() {
//        rtcEngine?.let {
//            RtcEngine.destroy()
//            rtcEngine = null
//        }
//    }
//}