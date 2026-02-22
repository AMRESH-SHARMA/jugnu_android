package com.example.app.core.call

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PendingIncomingCall(
    val callId: String,
    val callType: CallType,
    val callerAccountId: Long,
    val timestamp: Long // Server's startedAt timestamp
)

@Singleton
class PendingCallStore @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        private const val KEY_PENDING_CALL = "pending_incoming_call"
    }

    fun save(callId: String, callType: CallType, callerAccountId: Long, startedAt: Long) {
        val json = Json.encodeToString(
            PendingIncomingCall(callId, callType, callerAccountId, startedAt)
        )
        prefs.edit().putString(KEY_PENDING_CALL, json).apply()
    }

    /**
     * Returns the pending call ONCE and clears it.
     * Use this on app cold start.
     */
    fun consume(): PendingIncomingCall? {
        val json = prefs.getString(KEY_PENDING_CALL, null) ?: return null
        prefs.edit().remove(KEY_PENDING_CALL).apply()
        return runCatching {
            Json.decodeFromString<PendingIncomingCall>(json)
        }.getOrNull()
    }

    fun clear() {
        prefs.edit().remove(KEY_PENDING_CALL).apply()
    }

    fun peek(): PendingIncomingCall? {
        val json = prefs.getString(KEY_PENDING_CALL, null) ?: return null
        return runCatching {
            Json.decodeFromString<PendingIncomingCall>(json)
        }.getOrNull()
    }

    fun exists(callId: String): Boolean {
        return peek()?.callId == callId
    }
}
