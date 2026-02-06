package com.example.app.core.session

import com.example.app.core.preferences.user.domain.UserRole

object SessionManager {

    // From DataStore (persisted)
    var userAccountId: Long = 0L
    var userRole: UserRole = UserRole.CUSTOMER
    var fcmToken: String? = null

    // Runtime-only values (not persisted)
    var sessionId: String = ""
    //    var rtmToken: String = ""
    //    var rtcToken: String = ""
    //    var currentCallId: String = ""
    //    var isInCall: Boolean = false

    //    fun clearCallSession() {
    //        rtcToken = ""
    //        currentCallId = ""
    //        isInCall = false
    //    }
}