package com.example.app.feature.call.data

data class StartCallRequest(
    val callerId: String,
    val calleeId: String,
    val channel: String
)

data class AcceptCallRequest(
    val callId: String
)

data class RejectCallRequest(
    val callId: String
)

data class StartCallDto(
    val callId: String,
    val status: String
)

data class AcceptCallDto(
    val callId: String,
    val status: String,
    val channel: String
)

data class RejectCallDto(
    val callId: String,
    val status: String
)