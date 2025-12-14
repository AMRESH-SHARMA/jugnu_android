package com.example.app.core.call

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CallEventBus {
    private val _events = MutableSharedFlow<CallEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    fun emit(event: CallEvent) {
        _events.tryEmit(event)
    }
}
