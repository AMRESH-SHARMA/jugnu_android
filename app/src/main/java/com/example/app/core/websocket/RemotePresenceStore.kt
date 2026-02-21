package com.example.app.core.websocket

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePresenceStore @Inject constructor() {
    private val TAG = "RTM"

    private val _states = MutableStateFlow<Map<String, PresenceState>>(emptyMap())
    val states: StateFlow<Map<String, PresenceState>> = _states

    private val _availability = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val availability: StateFlow<Map<String, Boolean>> = _availability

    fun update(accountId: String, status: PresenceState) {
        Log.d(TAG, "RemotePresenceStore: update() - accountId=$accountId, status=$status")
        _states.value = _states.value.toMutableMap().apply {
            this[accountId] = status
        }
    }

    fun updateAvailability(accountId: String, isAvailable: Boolean) {
        Log.d(TAG, "RemotePresenceStore: updateAvailability() - accountId=$accountId, isAvailable=$isAvailable")
        _availability.value = _availability.value.toMutableMap().apply {
            this[accountId] = isAvailable
        }
    }

    fun get(accountId: String): PresenceState =
        _states.value[accountId] ?: PresenceState.OFFLINE

    fun getAvailability(accountId: String): Boolean? =
        _availability.value[accountId]
}
