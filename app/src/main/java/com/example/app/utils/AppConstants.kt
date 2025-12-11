package com.example.app.utils

object AppConstants {

    // =========================
    //        CALL EVENTS
    // =========================

    const val EVENT_INCOMING_CALL = "incoming_call"
    const val EVENT_CALL_ACCEPTED = "call_accepted"
    const val EVENT_CALL_REJECTED = "call_rejected"
    const val EVENT_CALL_CANCELLED = "call_cancelled"
    const val EVENT_CALL_ENDED = "call_ended"
    const val EVENT_CALL_MISSED = "call_missed"
    
    // Timeouts
    const val STATE_FLOW_STOP_TIMEOUT = 5000L
//    const val ONE_SECOND = 1000L
//    const val CALL_TIMER_INTERVAL = 1000L

    // User Defaults
//    const val DEFAULT_ACCOUNT_ID = ""
//    const val DEFAULT_DURATION_LABEL = "00:00"

    // DataStore Keys (optional)
    const val DATASTORE_FILE_NAME = "user_prefs.json"
}
