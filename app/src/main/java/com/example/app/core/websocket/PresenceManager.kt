package com.example.app.core.websocket

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

// BUSY is not a WebSocket concern — it's a domain rule (usually driven by calls).
@Singleton
class PresenceManager @Inject constructor(
    private val store: PresenceStore,
    private val wsManager: PresenceWebSocketManager
) {
    private val TAG = "APP:WS"

    /** Fired when WebSocket connects. */
    fun onConnected() {
        Log.d(TAG, "PresenceManager: onConnected() - current state=${store.state.value}")
        if (store.state.value == PresenceState.BUSY) return
        if (store.state.value != PresenceState.ONLINE) {
            store.setState(PresenceState.ONLINE)
        }
    }

    /** Fired when WebSocket disconnects. */
    fun onDisconnected() {
        Log.d(TAG, "PresenceManager: onDisconnected() - current state=${store.state.value}")
        if (store.state.value != PresenceState.OFFLINE) {
            store.setState(PresenceState.OFFLINE)
        }
    }

    /** Local call start → mark BUSY & notify server. */
    fun onCallStarted() {
        Log.d(TAG, "PresenceManager: onCallStarted() - current state=${store.state.value}")
        if (store.state.value != PresenceState.BUSY) {
            store.setState(PresenceState.BUSY)
            wsManager.sendCallStart()
        }
    }

    /** Local call end → notify server then update presence. */
    fun onCallEnded() {
        Log.d(TAG, "PresenceManager: onCallEnded() - current state=${store.state.value}")
        if (store.state.value == PresenceState.BUSY) {
            wsManager.sendCallEnd()
            store.setState(PresenceState.ONLINE)
        }
    }

    /** Presence driven from server broadcasts (remote changes). */
    fun onRemoteStateChanged(state: PresenceState) {
        Log.d(TAG, "PresenceManager: onRemoteStateChanged() - new state=$state, current=${store.state.value}")
        if (store.state.value != state) {
            store.setState(state)
        }
    }

    /** App goes to background - disconnect WebSocket */
    fun onAppBackground() {
        Log.d(TAG, "PresenceManager: onAppBackground()")
        wsManager.onAppBackground()
    }

    /** App comes to foreground - reconnect WebSocket */
    fun onAppForeground() {
        Log.d(TAG, "PresenceManager: onAppForeground()")
        wsManager.onAppForeground()
    }

    /** Network becomes available - reconnect if needed */
    fun onNetworkAvailable() {
        Log.d(TAG, "PresenceManager: onNetworkAvailable()")
        wsManager.onNetworkAvailable()
    }

    /** Network lost - notify server */
    fun onNetworkLost() {
        Log.d(TAG, "PresenceManager: onNetworkLost()")
        wsManager.onNetworkLost()
    }
}
