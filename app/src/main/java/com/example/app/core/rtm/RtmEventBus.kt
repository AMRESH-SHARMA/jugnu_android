package com.example.app.core.rtm

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RtmEventBus {

    private val _events = MutableSharedFlow<CallSignalPayload>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: CallSignalPayload) {
        _events.emit(event)
    }
}
