package com.example.app.core.rtc

import android.view.SurfaceView

interface VideoRenderer {
    fun bindLocal(surface: SurfaceView)
    fun bindRemote(uid: Int, surface: SurfaceView)
    fun switchCamera()
}


class AgoraVideoRenderer(
    private val rtc: AgoraVideoRtcManager
) : VideoRenderer {

    override fun bindLocal(surface: SurfaceView) {
        rtc.setupLocalVideo(surface)
    }

    override fun bindRemote(uid: Int, surface: SurfaceView) {
        rtc.setupRemoteVideo(uid, surface)
    }

    override fun switchCamera() {
        rtc.switchCamera()
    }
}
