package com.example.app.core.websocket

import kotlinx.serialization.Serializable

@Serializable
data class PresenceBroadcastMessage(
    val account_id: String,
    val status: String
)

// TODO
fun PresenceBroadcastMessage.toState(): PresenceState =
    when (status) {
        "ONLINE" -> PresenceState.ONLINE
        "BUSY" -> PresenceState.BUSY
        "OFFLINE" -> PresenceState.OFFLINE
        else -> PresenceState.OFFLINE
    }

