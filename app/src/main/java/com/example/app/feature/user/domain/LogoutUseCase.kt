package com.example.app.feature.user.domain

import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.session.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        // Clear persisted data
        userPreferencesRepository.clearSession()
        
        // Clear runtime session data
        SessionManager.userAccountId = 0L
        SessionManager.userRole = null
        SessionManager.sessionId = ""
        SessionManager.fcmToken = null
    }
}
