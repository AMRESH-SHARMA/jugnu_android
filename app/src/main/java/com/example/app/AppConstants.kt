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
    //        API TIMEOUTS
    // =========================

    const val API_CONNECT_TIMEOUT = 10L      // Connection timeout in seconds
    const val API_READ_TIMEOUT = 30L         // Read timeout in seconds
    const val API_WRITE_TIMEOUT = 30L        // Write timeout in seconds

    // =========================
    //        UI TIMEOUTS
    // =========================
    const val SNACKBAR_DURATION = 3000L  // 3 seconds
    
    // Filter debounce delay to prevent rapid API calls when switching filters quickly
    const val FILTER_DEBOUNCE_DELAY = 250L  // milliseconds

    // =========================
    //        CALL EVENTS
    // =========================

    const val EVENT_INCOMING_CALL = "incoming_call"
    const val EVENT_CALL_RECEIVED = "call_received"  // Callee confirms they received the call
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