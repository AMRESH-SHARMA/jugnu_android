package com.example.app.core.rtc

import kotlinx.coroutines.flow.Flow

interface RtcManager {
    val events: Flow<RtcEvent>
    fun join(channel: String, uid: Int)
    fun leave()
}
