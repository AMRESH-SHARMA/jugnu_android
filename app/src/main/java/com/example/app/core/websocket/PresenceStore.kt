package com.example.app.core.websocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceStore @Inject constructor() {
    /**
     * Tracks presence of the local logged-in user only
     * User can explicitly set their status Online / offline
     */
    private val _state = MutableStateFlow(PresenceState.OFFLINE)
    val state: StateFlow<PresenceState> = _state

    fun setState(newState: PresenceState) {
        _state.value = newState
    }

    fun isOnline(): Boolean =
        _state.value == PresenceState.ONLINE

    fun isBusy(): Boolean =
        _state.value == PresenceState.BUSY
}


