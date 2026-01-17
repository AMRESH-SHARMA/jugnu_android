package com.example.app.feature.call.domain.usecase

import android.util.Log
import com.example.app.AppConstants
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.core.session.SessionManager
import com.example.app.feature.call.data.CallRepository
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject

class EndCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {
    suspend operator fun invoke(
        callId: String,
        callerAccountId: Long,
        calleeAccountId: Long,
        callStatus: CallStatus,
        callType: CallType
    ): ApiResult<Unit> {

        Log.d(
            "RTM",
            "EndCall → callId=$callId status=$callStatus caller=$callerAccountId callee=$calleeAccountId"
        )

        val event = when (callStatus) {
            CallStatus.OUTGOING_RINGING,
            CallStatus.INCOMING_RINGING -> AppConstants.EVENT_CALL_CANCELLED

            CallStatus.CONNECTING,
            CallStatus.CONNECTED,
            CallStatus.ENDED -> AppConstants.EVENT_CALL_ENDED

            else -> AppConstants.EVENT_CALL_ENDED
        }

        // FAST PATH → notify remote user via RTM
        val remoteUserAccId =
            if (callerAccountId == SessionManager.userId) calleeAccountId
            else callerAccountId

        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(remoteUserAccId),
            payload = CallSignalPayload(
                event = event,
                callId = callId,
                callType = callType,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        // SLOW PATH → persist only on END
        return if (event == AppConstants.EVENT_CALL_ENDED) {
            when (val result = repo.endCall(callId)) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> ApiResult.Error(
                    message = result.message,
                    code = result.code,
                    exception = result.exception
                )
            }
        } else {
            // cancel call → nothing to persist
            ApiResult.Success(Unit)
        }
    }
}