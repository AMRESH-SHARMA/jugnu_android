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
            "RTM",
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
        
        val current = CallStore.current()
        if (current != null) {
            Log.w("RTM", "Incoming call rejected: already have active call ${current.callId}")
            return
        }

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
        
        // Validate transition
        if (!CallLifecycle.canTransition(current.status, CallStatus.OUTGOING_RINGING)) {
            Log.w("RTM", "Cannot transition to OUTGOING_RINGING from ${current.status}")
            return
        }
        
        // Only update if we're in the right state and it's the same call
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
        log("onAccepted()")
        val current = CallStore.current() ?: return

        // Validate state transition
        val transitionResult = CallLifecycle.transitionTo(
            currentStatus = current.status,
            newStatus = CallStatus.CONNECTING,
            callId = current.callId
        )
        
        if (transitionResult.isFailure) {
            Log.w("RTM", "Cannot accept call: ${transitionResult.exceptionOrNull()?.message}")
            return
        }

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
        val current = CallStore.current() ?: return
        
        // Validate transition
        if (!CallLifecycle.canTransition(current.status, CallStatus.CONNECTED)) {
            Log.w("RTM", "Cannot transition to CONNECTED from ${current.status}")
            return
        }
        
        CallStore.update {
            it.copy(status = CallStatus.CONNECTED)
        }
    }

    // ------------------------------------------------------------
    // TERMINATION
    // ------------------------------------------------------------
    fun onRejected() {
        log("onRejected()")
        val current = CallStore.current()
        
        // Validate transition if there's an active call
        if (current != null && !CallLifecycle.canTransition(current.status, CallStatus.REJECTED)) {
            Log.w("RTM", "Cannot reject call from ${current.status}")
            return
        }
        
        pendingCallStore.clear()
        CallStore.clear()
    }

    fun onEnded() {
        log("onEnded()")
        val current = CallStore.current()
        
        // Validate transition if there's an active call
        if (current != null && !CallLifecycle.canTransition(current.status, CallStatus.ENDED)) {
            Log.w("RTM", "Cannot end call from ${current.status}")
            return
        }
        
        pendingCallStore.clear()
        CallStore.clear()
    }

}