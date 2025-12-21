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
}