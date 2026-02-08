package com.example.app.core.session

import com.example.app.core.preferences.user.domain.UserRole

object SessionManager {

    // From DataStore (persisted)
    var userAccountId: Long = 0L
    var userRole: UserRole? = null  // Null until set after OTP verification
    var fcmToken: String? = null

    // Runtime-only values (not persisted)
    var sessionId: String = ""
}