package com.example.app.core.session

import com.example.app.core.preferences.user.domain.UserRole
import com.google.firebase.crashlytics.FirebaseCrashlytics

object SessionManager {

    // From DataStore (persisted)
    var userAccountId: Long = 0L
        set(value) {
            field = value
            // Update Crashlytics user ID when account ID changes
            if (value != 0L) {
                FirebaseCrashlytics.getInstance().setUserId(value.toString())
            }
        }
    var userRole: UserRole? = null  // Null until set after OTP verification
    var fcmToken: String? = null
    var isProfileComplete: Boolean = false

    // Runtime-only values (not persisted)
    var sessionId: String = ""
}