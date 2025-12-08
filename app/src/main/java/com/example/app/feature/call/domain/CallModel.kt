package com.example.app.feature.call.domain

data class CallModel(
    val callId: String,
    val status: String,
    val channel: String? = null
)