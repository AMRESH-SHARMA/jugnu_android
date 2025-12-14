package com.example.app.core.call

import android.util.Log
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor() {
    fun onIncoming(event: CallEvent.Incoming) {

        Log.d("RTM", "CallManager.onIncoming callId=${event.callId}")

        // Already in call → ignore or auto reject
        if (CallStore.current() != null) return

        CallStore.set(
            CallModel(
                callId = event.callId,
                status = CallStatus.INCOMING_RINGING,
                callerAccountId = event.callerAccountId,
                calleeAccountId = event.calleeAccountId,
                channel = event.channel
            )
        )
    }

    fun onOutgoing(call: CallModel) {
        CallStore.set(
            call.copy(status = CallStatus.OUTGOING_RINGING)
        )
    }

    fun onAccepted(event: CallEvent.Accepted) {
        CallStore.update {
            it.copy(
                status = CallStatus.CONNECTING,
                channel = event.channel
            )
        }
    }

    fun onConnected() {
        CallStore.update {
            it.copy(status = CallStatus.CONNECTED)
        }
    }

    fun onRejected() {
        CallStore.update {
            it.copy(status = CallStatus.ENDED)
        }
        CallStore.clear()
    }

    fun onEnded() {
        CallStore.update {
            it.copy(status = CallStatus.ENDED)
        }
        CallStore.clear()
    }
}
