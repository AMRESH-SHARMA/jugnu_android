package com.example.app.core.rtm

import kotlinx.serialization.Serializable

@Serializable
data class CallSignalPayload(
    val event: String,
    val callId: String,
    val callerAccountId: Long,
    val calleeAccountId: Long,
    val channel: String? = null
)
