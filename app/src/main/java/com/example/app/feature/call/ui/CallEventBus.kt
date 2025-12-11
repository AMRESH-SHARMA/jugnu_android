package com.example.app.feature.call.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CallEventBus {
    private val _events = MutableSharedFlow<CallEvent>()
    val events = _events.asSharedFlow()

    suspend fun send(event: CallEvent) {
        _events.emit(event)
    }
}

sealed class CallEvent {
    data class CallRejected(val callId: String?) : CallEvent()
    data class CallEnded(val callId: String?) : CallEvent()
    data class IncomingCall(val data: Map<String, String>) : CallEvent()
}
