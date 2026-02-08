package com.example.app.core.device

import com.example.app.core.device.domain.SendFcmTokenUseCase
import com.example.app.core.di.ApplicationScope
import com.example.app.core.session.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val session: UserSession,
    private val SendFcmTokenUseCase: SendFcmTokenUseCase,
    @ApplicationScope private val appScope: CoroutineScope
) {
    // To make it Idempotent
    private var started = false

    fun start() {
        if (started) return
        started = true

        appScope.launch(Dispatchers.IO) {
            combine(
                session.sessionIdFlow,
                session.fcmTokenFlow
            ) { sessionId, fcmToken ->
                Pair(sessionId, fcmToken)
            }
            .distinctUntilChanged()
            .collectLatest { (sessionId, fcmToken) ->
                if (sessionId.isNotBlank() && !fcmToken.isNullOrBlank()) {
                    SendFcmTokenUseCase(
                        sessionId = sessionId,
                        fcmToken = fcmToken
                    )
                }
            }
        }
    }
}