package com.example.app.core.device

import com.example.app.core.device.domain.SendDeviceTokenUseCase
import com.example.app.core.session.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val session: UserSession,
    private val sendTokenUseCase: SendDeviceTokenUseCase
) {
    // To make it Idempotent
    private var started = false

    fun start() {
        if (started) return
        started = true

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