package com.example.app.feature.call.data

import com.example.app.core.call.CallType
import com.example.app.feature.call.domain.CallStatus

// ---------------- REQUESTS ----------------

data class StartCallRequest(
    val callerAccountId: Long,
    val calleeAccountId: Long,
    val callType: CallType,
)

data class AcceptCallRequest(
    val callId: String
)

data class RejectCallRequest(
    val callId: String
)

data class CancelCallRequest(
    val callId: String
)

data class EndCallRequest(
    val callId: String
)

data class CallReceivedRequest(
    val callId: String,
    val calleeAccountId: Long
)


// ---------------- RESPONSES / DTOs ----------------

data class StartCallDto(
    val callId: String,
    val status: String,
    val channel: String? = null   // may not exist until accepted
)

data class AcceptCallDto(
    val callId: String,
    val channel: String,           // guaranteed after accept
    val status: CallStatus,
    val rtcToken: String
)

data class RejectCallDto(
    val callId: String,
    val status: String
)

data class CallStateDto(
    val callId: String,
    val status: String,
    val isActive: Boolean,
    val isExpired: Boolean,
    val startedAt: Long
)
