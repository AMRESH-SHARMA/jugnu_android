package com.example.app.core.user

import com.example.app.core.user.domain.model.UserRole
import com.example.app.core.user.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
//class UserSession @Inject constructor(
//    private val prefs: UserPreferencesRepository
//) {
//    var accountId: Long = 0L
//        private set
//
//    var role: UserRole = UserRole.CUSTOMER
//        private set
//
//    suspend fun initialize() {
//        val (id, r) = prefs.userPrefsFlow.first()
//        accountId = id
//        role = r
//    }
//}

@Singleton
class UserSession @Inject constructor(
    prefs: UserPreferencesRepository
) {
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sessionFlow = prefs.userPrefsFlow
        .stateIn(sessionScope, SharingStarted.Eagerly, Pair(0L, UserRole.CUSTOMER))

    val tokenFlow = prefs.tokenFlow
        .stateIn(sessionScope, SharingStarted.Eagerly, null)

    val accountId get() = sessionFlow.value.first
    val role get() = sessionFlow.value.second
    val fcmToken get() = tokenFlow.value
}


