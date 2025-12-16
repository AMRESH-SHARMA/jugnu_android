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

    const val START_RINGING_TIMEOUT = 30000L
    const val START_CONNECT_TIMEOUT = 30000L

    // DataStore Keys (optional)
    const val DATASTORE_FILE_NAME = "user_prefs.json"
}
