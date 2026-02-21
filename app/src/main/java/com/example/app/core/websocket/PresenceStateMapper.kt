package com.example.app.core.websocket

fun String.toPresenceState(): PresenceState {
    return when (this.uppercase()) {
        "ONLINE" -> PresenceState.ONLINE
        "BUSY" -> PresenceState.BUSY
        "OFFLINE" -> PresenceState.OFFLINE
        else -> PresenceState.OFFLINE
    }
}

fun PresenceSnapshotData.toPresenceState(): PresenceState {
    return this.status.toPresenceState()
}
