package com.example.app.feature.call.domain.usecase

import com.example.app.core.call.CallType
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import com.example.app.utils.AppConstants
import javax.inject.Inject

class AcceptCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {
    suspend operator fun invoke(
        callId: String,
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long
    ) {
        // 1️⃣ FAST PATH — notify caller via RTM
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(callerAccountId),
            payload = CallSignalPayload(
                event = AppConstants.EVENT_CALL_ACCEPTED,
                callId = callId,
                callType = callType,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        // 2️⃣ SLOW PATH — persist state in backend
        repo.acceptCall(callId)
    }
}


