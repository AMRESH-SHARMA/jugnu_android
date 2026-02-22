package com.example.app.core.session

import android.util.Log
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
            Log.d("RTM", "SessionInitializer Fetching profile for accountId=$accountId")
            when (val result = getCustomerProfile()) {
                is ApiResult.Success -> {
                    // Update from server (no caching)
                    SessionManager.isProfileComplete = result.data.isProfileComplete
                    Log.d("RTM", "SessionInitializer Profile loaded: isProfileComplete=${result.data.isProfileComplete}")
                }
                is ApiResult.Error -> {
                    // Default to true if API fails (assume profile complete unless proven otherwise)
                    SessionManager.isProfileComplete = true
                    Log.w("RTM", "SessionInitializer Profile fetch failed: ${result.message}, defaulting to true")
                }
            }
        } else {
            // Not logged in, default to true
            SessionManager.isProfileComplete = true
            Log.d("RTM", "SessionInitializer Not logged in, isProfileComplete=true")
        }
    }
}
