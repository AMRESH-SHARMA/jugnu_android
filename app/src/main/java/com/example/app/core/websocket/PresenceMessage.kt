package com.example.app.core.websocket

sealed class PresenceMessage {
    /**
     * Broadcast message sent by the server when any user's
     * presence changes. Example payload:
     * {"account_id":"123","status":"BUSY"}
     */
    data class PresenceUpdate(
        val account_id: String,
        val status: String
    ) : PresenceMessage()

    /**
     * Optional: represents a PONG reply from server.
     * (PING is sent by client → PONG received from server)
     */
    object Pong : PresenceMessage()
}
