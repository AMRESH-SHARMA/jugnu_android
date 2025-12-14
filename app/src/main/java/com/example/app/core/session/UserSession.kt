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

    val sessionFlow = prefs.userPrefsFlow
        .onEach { (id, role) ->
            SessionManager.userId = id      // update memory session
            SessionManager.userRole = role
        }
        .stateIn(sessionScope, SharingStarted.Companion.Eagerly, Pair(0L, UserRole.CUSTOMER))

    val tokenFlow = prefs.tokenFlow
        .onEach { token ->
            SessionManager.fcmToken = token // update memory session
        }
        .stateIn(sessionScope, SharingStarted.Companion.Eagerly, null)

    val accountId get() = sessionFlow.value.first
    val role get() = sessionFlow.value.second
    val fcmToken get() = tokenFlow.value
}