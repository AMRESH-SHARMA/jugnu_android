package com.example.app.core.websocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceStore @Inject constructor() {

    private val _state = MutableStateFlow(PresenceState.OFFLINE)
    val state: StateFlow<PresenceState> = _state

    fun setState(newState: PresenceState) {
        _state.value = newState
    }

    fun isOnline(): Boolean {
        return _state.value != PresenceState.OFFLINE
    }

    fun isBusy(): Boolean {
        return _state.value == PresenceState.BUSY
    }
}

