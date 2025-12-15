package com.example.app.core.rtc

import io.agora.rtc2.IRtcEngineEventHandler

class AgoraRtcListener(
    private val emitter: (RtcEvent) -> Unit
) : IRtcEngineEventHandler() {

    override fun onJoinChannelSuccess(
        channel: String?,
        uid: Int,
        elapsed: Int
    ) {
        emitter(RtcEvent.Connected)
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        emitter(RtcEvent.Disconnected)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        emitter(RtcEvent.RemoteJoined(uid))
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        emitter(RtcEvent.RemoteLeft(uid))
    }

    override fun onError(err: Int) {
        emitter(RtcEvent.Error(err))
    }
}
