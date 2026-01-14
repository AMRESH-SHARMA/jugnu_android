package com.example.app.core.call

sealed class CallEvent {
    // ---- Discovery ----
    data class Incoming(
        val callId: String,
        val callerAccountId: Long,
        val calleeAccountId: Long,
        val callType: CallType,
        val channel: String?
    ) : CallEvent()

    // ---- Local intent ----
    data class Outgoing(
        val callId: String,
        val callerAccountId: Long,
        val calleeAccountId: Long,
        val callType: CallType,
        val calleeName: String?,
        val calleeAvatar: String?
    ) : CallEvent()

    // ---- Signaling ----
    data class Accepted(
        val callId: String,
        val channel: String?,
        val rtcToken: String
    ) : CallEvent()

    // ---- Termination ----
    data class Connected(
        val callId: String
    ) : CallEvent()

    data class Rejected(
        val callId: String
    ) : CallEvent()

    data class Ended(val callId: String) : CallEvent()

    data class Cancelled(val callId: String, val calleeAccountId: Long?) : CallEvent()
}
