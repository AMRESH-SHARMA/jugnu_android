package com.example.app.core.session

import com.example.app.core.network.ApiResult
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.feature.user.domain.GetCustomerProfileUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionInitializer @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getCustomerProfile: GetCustomerProfileUseCase
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

        // If logged in, fetch fresh profile from server (source of truth)
        if (sessionId.isNotEmpty() && accountId != 0L) {
            when (val result = getCustomerProfile()) {
                is ApiResult.Success -> {
                    // Update from server
                    SessionManager.isProfileComplete = result.data.isProfileComplete
                    userPreferencesRepository.saveProfileComplete(result.data.isProfileComplete)
                }
                is ApiResult.Error -> {
                    // Fallback to cached value if API fails
                    val cachedValue = userPreferencesRepository.isProfileCompleteFlow.first()
                    SessionManager.isProfileComplete = cachedValue
                }
            }
        } else {
            // Not logged in, load cached value
            val isProfileComplete = userPreferencesRepository.isProfileCompleteFlow.first()
            SessionManager.isProfileComplete = isProfileComplete
        }
    }
}
