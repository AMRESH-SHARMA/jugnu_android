package com.example.app.core.call

import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor() {

    // ------------------------------------------------------------
    // OUTGOING
    // ------------------------------------------------------------

    fun onOutgoing(call: CallModel) {
        CallStore.set(
            call.copy(status = CallStatus.OUTGOING_RINGING)
        )
    }

    // ------------------------------------------------------------
    // INCOMING (RTM)
    // ------------------------------------------------------------

    fun onIncoming(event: CallEvent.Incoming) {
        if (CallStore.current() != null) return

        CallStore.set(
            CallModel(
                callId = event.callId,
                status = CallStatus.INCOMING_RINGING,
                callerAccountId = event.callerAccountId,
                calleeAccountId = event.calleeAccountId,
                callType = event.callType,
                channel = event.channel
            )
        )
    }

    // ------------------------------------------------------------
    // ACCEPTED (RTM)
    // ------------------------------------------------------------

    fun onAccepted(event: CallEvent.Accepted) {
        CallStore.update {
            it.copy(
                status = CallStatus.CONNECTING,
                channel = event.channel
            )
        }
    }

    // ------------------------------------------------------------
    // RTC STATES
    // ------------------------------------------------------------

    fun onConnecting() {
        CallStore.update {
            it.copy(status = CallStatus.CONNECTING)
        }
    }

    fun onConnected() {
        CallStore.update {
            it.copy(status = CallStatus.CONNECTED)
        }
    }

    // ------------------------------------------------------------
    // TERMINATION
    // ------------------------------------------------------------

    fun onRejected() {
        CallStore.clear()
    }

    fun onEnded() {
        CallStore.clear()
    }

    // ------------------------------------------------------------
    // ENDED (RTM)
    // ------------------------------------------------------------
//    fun onEnded(event: CallEvent) {
//        val current = CallStore.current() ?: return
//
//        val eventCallId = when (event) {
//            is CallEvent.Ended -> event.callId
//            is CallEvent.Cancelled -> event.callId
//            is CallEvent.Missed -> event.callId
//            is CallEvent.Rejected -> event.callId
//            else -> return
//        }
//
//        if (current.callId != eventCallId) return
//
//        CallStore.clear()
//    }


}