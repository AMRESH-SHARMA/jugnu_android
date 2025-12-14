package com.example.app.feature.call.domain.usecase

import android.util.Log
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import com.example.app.feature.call.domain.CallStatus
import com.example.app.utils.AppConstants
import javax.inject.Inject

class EndCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {
    suspend operator fun invoke(
        callId: String,
        callerAccountId: Long,
        calleeAccountId: Long,
        callStatus: CallStatus
    ) {
        Log.d(
            "RTM",
            "CALL EndCall invoked callId=$callId status=$callStatus " +
                    "caller=$callerAccountId callee=$calleeAccountId"
        )

        val event = when (callStatus) {
            CallStatus.OUTGOING_RINGING,
            CallStatus.INCOMING_RINGING -> AppConstants.EVENT_CALL_CANCELLED

            CallStatus.CONNECTING,
            CallStatus.CONNECTED,
            CallStatus.ENDED -> AppConstants.EVENT_CALL_ENDED

            else -> AppConstants.EVENT_CALL_ENDED
        }

        Log.d("RTM", "CALL Sending RTM event=$event")
        // 1️⃣ FAST PATH — notify callee immediately
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(calleeAccountId),
            payload = CallSignalPayload(
                event = event,
                callId = callId,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        // 2️⃣ SLOW PATH — persist only if accepted
        if (event == AppConstants.EVENT_CALL_ENDED) {
            repo.endCall(callId)
        }
    }
}


