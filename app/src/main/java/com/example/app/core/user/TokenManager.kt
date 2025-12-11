package com.example.app.core.user

import com.example.app.core.device.domain.SendDeviceTokenUseCase
import com.example.app.core.user.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val session: UserSession,
    private val prefs: UserPreferencesRepository,
    private val sendTokenUseCase: SendDeviceTokenUseCase
) {
    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            combine(
                session.sessionFlow,
                session.tokenFlow
            ) { sessionData, token ->
                Pair(sessionData.first, token)
            }.collect { (accountId, token) ->

                if (accountId > 0 && !token.isNullOrBlank()) {
                    sendTokenUseCase(accountId, token)
                }
            }
        }
    }
}
