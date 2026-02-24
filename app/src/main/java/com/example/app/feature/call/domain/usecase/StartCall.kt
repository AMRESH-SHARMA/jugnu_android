package com.example.app.feature.call.domain.usecase

import com.example.app.AppConstants
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject

class StartCall @Inject constructor(
    private val repo: CallRepository,
//    private val rtmCallSignaling: RtmCallSignaling
) {

    suspend operator fun invoke(
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long,
        calleeName: String,
        calleeAvatar: String?
    ): ApiResult<CallModel> {

        // Backend creates call + sends RTM to callee
        val result = repo.startCall(
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId,
            callType = callType
        )

        return when (result) {
            is ApiResult.Success -> {
                val dto = result.data

                // FAST PATH — notify callee
//                rtmCallSignaling.sendCallEvent(
//                    channel = RtmChannels.user(calleeAccountId),
//                    payload = CallSignalPayload(
//                        event = AppConstants.EVENT_INCOMING_CALL,
//                        callId = dto.callId,
//                        callType = callType,
//                        callerAccountId = callerAccountId,
//                        calleeAccountId = calleeAccountId,
//                        channel = dto.channel
//                    )
//                )

                // Build domain model for caller UI
                // Return OUTGOING_CONNECTING - will change to OUTGOING_RINGING when callee acknowledges
                ApiResult.Success(
                    CallModel(
                        callId = dto.callId,
                        status = CallStatus.OUTGOING_CONNECTING,
                        callType = callType,
                        channel = dto.channel,
                        callerAccountId = callerAccountId,
                        calleeAccountId = calleeAccountId,
                        calleeName = calleeName,
                        calleeAvatar = calleeAvatar,
                        rtcToken = null
                    )
                )
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