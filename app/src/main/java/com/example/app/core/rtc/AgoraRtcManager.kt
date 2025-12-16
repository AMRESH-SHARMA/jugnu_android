//package com.example.app.core.rtc
//
//import android.content.Context
//import android.util.Log
//import dagger.hilt.android.qualifiers.ApplicationContext
//import io.agora.rtc2.IRtcEngineEventHandler
//import io.agora.rtc2.RtcEngine
//import io.agora.rtc2.RtcEngineConfig
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class AgoraRtcManager @Inject constructor(
//    @ApplicationContext context: Context
//) : RtcManager {
//
//    private val _events = MutableSharedFlow<RtcEvent>()
//    override val events = _events.asSharedFlow()
//
//    private val rtcEngine: RtcEngine
//
//    init {
//        val config = RtcEngineConfig().apply {
//            mContext = context
//            mAppId = "YOUR_APP_ID"
//            mEventHandler = object : IRtcEngineEventHandler() {
//
//                override fun onJoinChannelSuccess(
//                    channel: String?,
//                    uid: Int,
//                    elapsed: Int
//                ) {
//                    Log.d("RTM", "LOCAL joined channel=$channel uid=$uid")
//                    _events.tryEmit(RtcEvent.Connected)
//                }
//
//                override fun onLeaveChannel(stats: RtcStats?) {
//                    Log.d("RTM", "LOCAL left channel: $stats ")
//                    _events.tryEmit(RtcEvent.Disconnected)
//                }
//
//                override fun onError(err: Int) {
//                    Log.d("RTM", "LOCAL error channel: $err ")
//                    _events.tryEmit(RtcEvent.Error(err))
//                }
//            }
//        }
//        rtcEngine = RtcEngine.create(config)
//    }
//
//    override fun join(channel: String, uid: Int) {
//        rtcEngine.joinChannel(null, channel, null, uid)
//    }
//
//    override fun leave() {
//        rtcEngine.leaveChannel()
//    }
//}
