package com.example.app.core.call

import android.util.Log
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor() {

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
                status = CallStatus.OUTGOING_RINGING,
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
    // ACCEPTED (RTM)
    // ------------------------------------------------------------

    fun onAccepted(event: CallEvent.Accepted) {
        log("onAccepted()")
        CallStore.update {
            it.copy(
                status = CallStatus.CONNECTING,
                channel = event.channel,
                rtcToken = event.rtcToken
            )
        }
    }

    // ------------------------------------------------------------
    // RTC STATES
    // ------------------------------------------------------------

    fun onConnecting() {
        log("onConnecting()")
        CallStore.update {
            it.copy(status = CallStatus.CONNECTING)
        }
    }

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
        CallStore.clear()
    }

    fun onEnded() {
        log("onEnded()")
        CallStore.clear()
    }
}