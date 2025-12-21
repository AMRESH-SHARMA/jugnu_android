package com.example.app.core.websocket

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePresenceStore @Inject constructor() {

    private val _states = MutableStateFlow<Map<String, PresenceState>>(emptyMap())
    val states: StateFlow<Map<String, PresenceState>> = _states

    fun update(accountId: String, status: PresenceState) {
        Log.d("RTM", "PRESENCE update $accountId -> $status")
        _states.value = _states.value.toMutableMap().apply {
            this[accountId] = status
        }
    }

    fun get(accountId: String): PresenceState =
        _states.value[accountId] ?: PresenceState.OFFLINE
}
