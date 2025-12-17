package com.example.app.core.session

import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.preferences.user.domain.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(
    prefs: UserPreferencesRepository
) {
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // -------------------------
    // Persisted user session
    // -------------------------
    val sessionFlow = prefs.userPrefsFlow
        .onEach { (id, role) ->
            // Update in-memory cache
            SessionManager.userId = id
            SessionManager.userRole = role
        }
        .stateIn(sessionScope, SharingStarted.Companion.Eagerly, Pair(0L, UserRole.CUSTOMER))

    // -------------------------
    // Persisted FCM token
    // -------------------------
    val tokenFlow = prefs.tokenFlow
        .onEach { token ->
            SessionManager.fcmToken = token // Update in-memory cache
        }
        .stateIn(sessionScope, SharingStarted.Companion.Eagerly, null)

    // -------------------------
    // Simple getters (sync)
    // -------------------------
    val accountId: Long
        get() = sessionFlow.value.first

    val role: UserRole
        get() = sessionFlow.value.second

    val fcmToken: String?
        get() = tokenFlow.value

    // -------------------------
    // Derived session state
    // -------------------------
    fun isLoggedIn(): Boolean = accountId > 0
}