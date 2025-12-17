package com.example.app.core.websocket

sealed class PresenceMessage {
    object Ping
    data class Status(val state: String)
}
