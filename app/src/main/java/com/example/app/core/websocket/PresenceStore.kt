package com.example.app.core.websocket

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceStore @Inject constructor() {
    private val TAG = "APP:WS"
    
    /**
     * Tracks presence of the local logged-in user only
     * User can explicitly set their status Online / offline
     */
    private val _state = MutableStateFlow(PresenceState.OFFLINE)
    val state: StateFlow<PresenceState> = _state

    fun setState(newState: PresenceState) {
        Log.d(TAG, "PresenceStore: setState() - ${_state.value} -> $newState")
        _state.value = newState
    }

    fun isOnline(): Boolean =
        _state.value == PresenceState.ONLINE

    fun isBusy(): Boolean =
        _state.value == PresenceState.BUSY
}


