package com.example.app.core.call

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/*
* CallEventBus is a one-way event pipe that delivers call events to the single state owner (CallManager).
* CallEventBus = mailbox
* CallManager = decision maker
* */

object CallEventBus {
    private val _events = MutableSharedFlow<CallEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    fun emit(event: CallEvent) {
        _events.tryEmit(event)
    }
}
