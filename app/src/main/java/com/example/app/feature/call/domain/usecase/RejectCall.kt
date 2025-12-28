package com.example.app.feature.call.domain.usecase

import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import com.example.app.utils.AppConstants
import javax.inject.Inject

class RejectCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {
    suspend operator fun invoke(
        callId: String,
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long
    ): ApiResult<Unit> {

        // FAST PATH → notify caller immediately
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(callerAccountId),
            payload = CallSignalPayload(
                event = AppConstants.EVENT_CALL_REJECTED,
                callId = callId,
                callType = callType,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        // SLOW PATH → persist rejection
        return when (val result = repo.rejectCall(callId)) {
            is ApiResult.Success -> {
                ApiResult.Success(Unit) // nothing to return to VM
            }

            is ApiResult.Error -> {
                ApiResult.Error(
                    message = result.message,
                    code = result.code,
                    exception = result.exception
                )
            }
        }
    }
}


/*
class RejectCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {
    suspend operator fun invoke(
        callId: String,
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long
    ) {
        // 1️⃣ FAST PATH — notify caller immediately
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(callerAccountId),
            payload = CallSignalPayload(
                event = AppConstants.EVENT_CALL_REJECTED,
                callId = callId,
                callType = callType,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        // 2️⃣ SLOW PATH — persist rejection in backend
        repo.rejectCall(callId)
    }
}
*/
