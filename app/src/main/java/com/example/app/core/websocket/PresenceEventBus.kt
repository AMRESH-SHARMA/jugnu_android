package com.example.app.core.websocket

import kotlinx.coroutines.flow.MutableSharedFlow

object PresenceEventBus {
    val events = MutableSharedFlow<PresenceEvent>()
}