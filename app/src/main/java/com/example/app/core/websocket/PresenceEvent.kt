package com.example.app.core.websocket

sealed class PresenceEvent {
    object Connected : PresenceEvent()
    object Disconnected : PresenceEvent()
    data class StatusChanged(val state: PresenceState) : PresenceEvent()
}
