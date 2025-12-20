package com.example.app.core.websocket

import javax.inject.Inject
import javax.inject.Singleton

// BUSY is not a WebSocket concern — it’s a domain rule (usually driven by calls).
@Singleton
class PresenceManager @Inject constructor(
    private val store: PresenceStore,
    private val wsManager: PresenceWebSocketManager
) {

    /** Fired when WebSocket connects. */
    fun onConnected() {
        if (store.state.value == PresenceState.BUSY) return
        if (store.state.value != PresenceState.ONLINE) {
            store.setState(PresenceState.ONLINE)
        }
    }

    /** Fired when WebSocket disconnects. */
    fun onDisconnected() {
        if (store.state.value != PresenceState.OFFLINE) {
            store.setState(PresenceState.OFFLINE)
        }
    }

    /** Local call start → mark BUSY & notify server. */
    fun onCallStarted() {
        if (store.state.value != PresenceState.BUSY) {
            store.setState(PresenceState.BUSY)
            wsManager.sendCallStart()
        }
    }

    /** Local call end → notify server then update presence. */
    fun onCallEnded() {
        if (store.state.value == PresenceState.BUSY) {
            wsManager.sendCallEnd()
            store.setState(PresenceState.ONLINE)
        }
    }

    /** Presence driven from server broadcasts (remote changes). */
    fun onRemoteStateChanged(state: PresenceState) {
        if (store.state.value != state) {
            store.setState(state)
        }
    }

    // Future: app lifecycle integration
    // fun onAppForeground()
    // fun onAppBackground()

    // Future: network monitor integration
    // fun onNetworkLost()
    // fun onNetworkAvailable()

    // Future: appear offline toggle
    // fun onAppearOfflineEnabled()
    // fun onAppearOfflineDisabled()
}

//@Singleton
//class PresenceManager @Inject constructor(
//    private val store: PresenceStore,
//    private val wsManager: PresenceWebSocketManager
//) {
//
//    /** WebSocket connected */
//    fun onConnected() {
//        // If already BUSY, do NOT override it
//        if (store.state.value == PresenceState.BUSY) return
//
//        if (store.state.value != PresenceState.ONLINE) {
//            store.setState(PresenceState.ONLINE)
//        }
//    }
//
//    /** WebSocket disconnected */
//    fun onDisconnected() {
//        // OFFLINE always wins
//        if (store.state.value != PresenceState.OFFLINE) {
//            store.setState(PresenceState.OFFLINE)
//        }
//    }
//
//    /** Call started (Incoming or Outgoing) */
//    fun onCallStarted() {
//        if (store.state.value != PresenceState.BUSY) {
//            store.setState(PresenceState.BUSY)
//            wsManager.sendCallStarted()
//        }
//    }
//
//    /** Call ended */
//    fun onCallEnded() {
//        // If socket is alive → ONLINE
//        // If socket is dead → OFFLINE
//        when (store.state.value) {
//            PresenceState.BUSY -> {
//                wsManager.sendCallEnded()
//                store.setState(PresenceState.ONLINE)
//            }
//
//            else -> Unit
//        }
//    }
//
//    /** Future: server-driven state */
//    fun onRemoteStateChanged(state: PresenceState) {
//        if (store.state.value != state) {
//            store.setState(state)
//        }
//    }
//
//    /** TODO
//     * WebSocket may stay connected while app is backgrounded.
//    Presence must still change. */
////    fun onAppForeground()
////    fun onAppBackground()
//
//    /** TODO
//     * WebSocket disconnect is often late.
//     * Network loss must immediately clear BUSY */
////    fun onNetworkLost()
////    fun onNetworkAvailable()
//
//    /** TODO
//     * This is a business decision, not a transport event. */
////    fun onAppearOfflineEnabled()
////    fun onAppearOfflineDisabled()
//
//
//}
//
