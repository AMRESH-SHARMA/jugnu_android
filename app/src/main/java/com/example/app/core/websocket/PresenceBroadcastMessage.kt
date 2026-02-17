package com.example.app.core.websocket

import kotlinx.serialization.Serializable

@Serializable
data class PresenceBroadcastMessage(
    val account_id: String,
    val status: String
)

@Serializable
data class PresenceSnapshotMessage(
    val type: String,
    val data: Map<String, String>
)

@Serializable
data class ConnectionReplacedMessage(
    val type: String,
    val message: String? = null
)

// TODO
fun PresenceBroadcastMessage.toState(): PresenceState =
    when (status) {
        "ONLINE" -> PresenceState.ONLINE
        "BUSY" -> PresenceState.BUSY
        "OFFLINE" -> PresenceState.OFFLINE
        else -> PresenceState.OFFLINE
    }

