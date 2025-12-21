package com.example.app.core.websocket

sealed class PresenceEvent {

    object Connected : PresenceEvent()

    /**
     * Fired when WebSocket disconnects (network lost, server down, etc).
     * PresenceManager should mark local presence as Offline.
     */
    object Disconnected : PresenceEvent()

    data class StatusChanged(
        val accountId: String,
        val state: PresenceState
    ) : PresenceEvent()
}
