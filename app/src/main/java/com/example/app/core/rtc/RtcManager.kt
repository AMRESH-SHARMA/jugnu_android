package com.example.app.core.rtc

//import android.content.Context
//import io.agora.rtc2.RtcEngine
//
//object RtcManager {
//    private lateinit var engine: RtcEngine
//
//    fun init(context: Context, appId: String) {
//        engine = RtcEngine.create(context, appId, RtcEventHandler())
//    }
//
//    fun join(channel: String) {
//        engine.joinChannel(Session.rtcToken, channel, "", Session.userId.toInt())
//    }
//
//    fun leave() {
//        engine.leaveChannel()
//    }
//}