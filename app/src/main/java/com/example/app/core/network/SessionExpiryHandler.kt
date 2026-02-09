package com.example.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SessionExpiryHandler {
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(replay = 0)
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent
    
    suspend fun notifySessionExpired() {
        _sessionExpiredEvent.emit(Unit)
    }
}
