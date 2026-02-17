package com.example.app.core.call

import android.util.Log
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor(
    private val pendingCallStore: PendingCallStore
) {

    private fun log(action: String) {
        val current = CallStore.current()
        Log.w(
            "CALL_MANAGER",
            "$action | currentStatus=${current?.status} callId=${current?.callId} thread=${Thread.currentThread().name}"
        )
    }

    // ------------------------------------------------------------
    // OUTGOING
    // ------------------------------------------------------------

    fun onOutgoing(event: CallEvent.Outgoing) {
        CallStore.set(
            CallModel(
                callId = event.callId,
                status = CallStatus.OUTGOING_CONNECTING,  // Start with CONNECTING
                callType = event.callType,
                callerAccountId = event.callerAccountId,
                calleeAccountId = event.calleeAccountId,
                calleeName = event.calleeName,
                calleeAvatar = event.calleeAvatar
            )
        )
    }


    // ------------------------------------------------------------
    // INCOMING (RTM)
    // ------------------------------------------------------------

    fun onIncoming(event: CallEvent.Incoming) {
        log("onIncoming()")
        if (CallStore.current() != null) return

        CallStore.set(
            CallModel(
                callId = event.callId,
                status = CallStatus.INCOMING_RINGING,
                callType = event.callType,
                channel = event.channel,
                callerAccountId = event.callerAccountId,
                calleeAccountId = event.calleeAccountId,
            )
        )
    }

    // ------------------------------------------------------------
    // CALL RECEIVED (Callee acknowledges receiving the call)
    // ------------------------------------------------------------
    fun onCallReceived(event: CallEvent.CallReceived) {
        log("onCallReceived()")
        val current = CallStore.current() ?: return
        
        // Only update if we're still in OUTGOING_CONNECTING state
        if (current.callId == event.callId && current.status == CallStatus.OUTGOING_CONNECTING) {
            CallStore.update {
                it.copy(status = CallStatus.OUTGOING_RINGING)
            }
        }
    }

    // ------------------------------------------------------------
    // ACCEPTED (RTM)
    // ------------------------------------------------------------
    fun onAccepted(event: CallEvent.Accepted) {
        val current = CallStore.current() ?: return

        val newChannel = event.channel ?: current.channel
        val newToken = event.rtcToken.ifBlank { current.rtcToken }

        // 🔒 IDENTITY CHECK — nothing new to apply
        if (
            current.status == CallStatus.CONNECTING &&
            current.channel == newChannel &&
            current.rtcToken == newToken
        ) {
            return
        }

        CallStore.update {
            it.copy(
                status = CallStatus.CONNECTING,
                channel = newChannel,
                rtcToken = newToken
            )
        }
    }

    // ------------------------------------------------------------
    // RTC STATES
    // ------------------------------------------------------------
    fun onConnected() {
        log("onConnected()")
        CallStore.update {
            it.copy(status = CallStatus.CONNECTED)
        }
    }

    // ------------------------------------------------------------
    // TERMINATION
    // ------------------------------------------------------------
    fun onRejected() {
        log("onRejected()")
        pendingCallStore.clear()
        CallStore.clear()
    }

    fun onEnded() {
        log("onEnded()")
        pendingCallStore.clear()
        CallStore.clear()
    }

}