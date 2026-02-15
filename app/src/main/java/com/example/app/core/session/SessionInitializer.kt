package com.example.app.core.session

import com.example.app.core.preferences.user.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionInitializer @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun loadSession() {
        // Load user prefs (accountId and role)
        val (accountId, role) = userPreferencesRepository.userPrefsFlow.first()
        SessionManager.userAccountId = accountId
        SessionManager.userRole = role

        // Load session ID
        val sessionId = userPreferencesRepository.sessionIdFlow.first()
        SessionManager.sessionId = sessionId

        // Load FCM token
        val fcmToken = userPreferencesRepository.fcmTokenFlow.first()
        SessionManager.fcmToken = fcmToken

        // Load profile completion status
        val isProfileComplete = userPreferencesRepository.isProfileCompleteFlow.first()
        SessionManager.isProfileComplete = isProfileComplete
    }
}
