package com.example.app.utils

object AppConstants {

    // =========================
    //        API URL
    // =========================

    const val REMOTE_CONFIG_URL = "https://json.extendsclass.com/bin/09ed15210b50"
    const val USE_DEFAULT_URL = true
    const val DEFAULT_BASE_URL = "http://10.100.238.61:3001/api/v1/"
    const val WS_PRESENCE_PATH = "ws/presence"

    // =========================
    //        CALL EVENTS
    // =========================

    const val EVENT_INCOMING_CALL = "incoming_call"
    const val EVENT_CALL_ACCEPTED = "call_accepted"
    const val EVENT_CALL_REJECTED = "call_rejected"
    const val EVENT_CALL_CANCELLED = "call_cancelled"
    const val EVENT_CALL_ENDED = "call_ended"

    // Timeouts
    const val STATE_FLOW_STOP_TIMEOUT = 5000L

    const val START_RINGING_TIMEOUT = 30000L
    const val START_CONNECT_TIMEOUT = 30000L

    // DataStore Keys (optional)
    const val DATASTORE_FILE_NAME = "user_prefs.json"
}
