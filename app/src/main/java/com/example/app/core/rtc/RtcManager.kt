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
}
