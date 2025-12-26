package com.example.app.core.rtc

import kotlinx.coroutines.flow.Flow

interface RtcManager {
    val events: Flow<RtcEvent>
    fun join(
        callId: String,
        channel: String,
        token: String?
    )

    fun leave()

    fun muteLocalAudio(mute: Boolean)
    fun enableSpeaker(enable: Boolean)
}
