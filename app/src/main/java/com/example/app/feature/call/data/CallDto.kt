package com.example.app.feature.call.data

// ---------------- REQUESTS ----------------

data class StartCallRequest(
    val callerAccountId: Long,
    val calleeAccountId: Long
)

data class AcceptCallRequest(
    val callId: String
)

data class RejectCallRequest(
    val callId: String
)

data class EndCallRequest(
    val callId: String
)


// ---------------- RESPONSES / DTOs ----------------

data class StartCallDto(
    val callId: String,
    val status: String,
    val channel: String? = null   // may not exist until accepted
)

data class AcceptCallDto(
    val callId: String,
    val status: String,
    val channel: String           // guaranteed after accept
)

data class RejectCallDto(
    val callId: String,
    val status: String
)
