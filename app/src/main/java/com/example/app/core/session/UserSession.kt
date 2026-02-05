package com.example.app.core.session

import com.example.app.core.di.ApplicationScope
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.preferences.user.domain.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(
    prefs: UserPreferencesRepository,
    @ApplicationScope private val appScope: CoroutineScope
) {
    // -------------------------
    // Persisted user session
    // -------------------------
    val sessionFlow = prefs.userPrefsFlow
        .onEach { (id, role) ->
            SessionManager.userAccountId = id
            SessionManager.userRole = role
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = Pair(0L, UserRole.CUSTOMER)
        )

    // -------------------------
    // Persisted FCM token
    // -------------------------
    val fcmTokenFlow = prefs.fcmTokenFlow
        .onEach { fcmToken  ->
            SessionManager.fcmToken = fcmToken
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // -------------------------
    // Simple getters (sync)
    // -------------------------
    val accountId: Long
        get() = sessionFlow.value.first

    val role: UserRole
        get() = sessionFlow.value.second

    val fcmToken: String?
        get() = fcmTokenFlow.value

    // -------------------------
    // Derived session state
    // -------------------------
    fun isLoggedIn(): Boolean = accountId > 0
}