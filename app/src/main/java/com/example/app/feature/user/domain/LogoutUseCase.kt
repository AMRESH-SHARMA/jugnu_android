package com.example.app.feature.user.domain

import android.util.Log
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.session.SessionManager
import com.example.app.feature.user.data.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        try {
            // Try to invalidate server session
            userRepository.logout()
            Log.d("APP:LOGOUT", "Server logout successful")
        } catch (e: Exception) {
            // Log error but don't throw - continue with local cleanup
            Log.w("APP:LOGOUT", "Server logout failed, clearing local data anyway", e)
        } finally {
            // Always clear local data regardless of server response
            userPreferencesRepository.clearSession()
            
            // Clear runtime session data
            SessionManager.userAccountId = 0L
            SessionManager.userRole = null
            SessionManager.sessionId = ""
            SessionManager.fcmToken = null
            SessionManager.isProfileComplete = false
        }
    }
}
