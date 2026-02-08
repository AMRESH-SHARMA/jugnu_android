package com.example.app

object AppConstants {

    // =========================
    //        API URL
    // =========================

    const val REMOTE_CONFIG_URL = "https://storage.googleapis.com/jugnu-config-server/config.json"
    const val USE_DEFAULT_URL = true
    const val DEFAULT_BASE_URL = "http://192.168.1.6:3001/api/v1/"
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