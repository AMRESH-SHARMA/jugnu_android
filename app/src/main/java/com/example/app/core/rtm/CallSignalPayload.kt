package com.example.app.core.rtm

import com.example.app.core.call.CallType
import kotlinx.serialization.Serializable

@Serializable
data class CallSignalPayload(
    val event: String,
    val callId: String,
    val callType: CallType,
    val callerAccountId: Long,
    val calleeAccountId: Long,
    val channel: String? = null
)
