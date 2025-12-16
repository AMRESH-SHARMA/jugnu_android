package com.example.app.core.call

sealed class CallEvent {

    data class Incoming(
        val callId: String,
        val callerAccountId: Long,
        val calleeAccountId: Long,
        val callType: CallType,
        val channel: String? = null
    ) : CallEvent()

    data class Accepted(
        val callId: String,
        val channel: String?,
        val rtcToken: String
    ) : CallEvent()

    data class Rejected(
        val callId: String
    ) : CallEvent()

    data class Ended(
        val callId: String
    ) : CallEvent()

    data class Cancelled(
        val callId: String
    ) : CallEvent()

    data class Missed(
        val callId: String
    ) : CallEvent()
}
