package com.example.app.core.websocket

import javax.inject.Inject
import javax.inject.Singleton

// BUSY is not a WebSocket concern — it’s a domain rule (usually driven by calls).
@Singleton
class PresenceManager @Inject constructor(
    private val store: PresenceStore,
    private val wsManager: PresenceWebSocketManager
) {

    /** WebSocket connected */
    fun onConnected() {
        // If already BUSY, do NOT override it
        if (store.state.value == PresenceState.BUSY) return

        if (store.state.value != PresenceState.ONLINE) {
            store.setState(PresenceState.ONLINE)
        }
    }

    /** WebSocket disconnected */
    fun onDisconnected() {
        // OFFLINE always wins
        if (store.state.value != PresenceState.OFFLINE) {
            store.setState(PresenceState.OFFLINE)
        }
    }

    /** Call started (Incoming or Outgoing) */
    fun onCallStarted() {
        if (store.state.value != PresenceState.BUSY) {
            store.setState(PresenceState.BUSY)
            wsManager.sendCallStarted()
        }
    }

    /** Call ended */
    fun onCallEnded() {
        // If socket is alive → ONLINE
        // If socket is dead → OFFLINE
        when (store.state.value) {
            PresenceState.BUSY -> {
                wsManager.sendCallEnded()
                store.setState(PresenceState.ONLINE)
            }

            else -> Unit
        }
    }

    /** Future: server-driven state */
    fun onRemoteStateChanged(state: PresenceState) {
        if (store.state.value != state) {
            store.setState(state)
        }
    }
}

