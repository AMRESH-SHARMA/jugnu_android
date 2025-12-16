package com.example.app.core.rtm

import com.example.app.core.call.CallType
import kotlinx.serialization.Serializable

@Serializable
data class CallSignalPayload(
    val event: String,
    val callId: String,
    val callType: CallType? = null,
    val callerAccountId: Long? = null,
    val calleeAccountId: Long? = null,
    val channel: String? = null,
    val rtcToken: String? = null
)
