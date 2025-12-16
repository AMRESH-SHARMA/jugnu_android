package com.example.app.feature.call.domain.usecase

import com.example.app.core.call.CallType
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import com.example.app.utils.AppConstants
import javax.inject.Inject

class StartCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {

    suspend operator fun invoke(
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long,
        calleeName: String,
        calleeAvatar: String?
    ): CallModel {

        // 1️⃣ SLOW PATH FIRST — backend creates call (SOURCE OF TRUTH)
        val dto = repo.startCall(
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId,
            callType = callType
        )

        // 2️⃣ FAST PATH — notify callee via RTM
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(calleeAccountId),
            payload = CallSignalPayload(
                event = AppConstants.EVENT_INCOMING_CALL,
                callId = dto.callId,
                callType = callType,
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId,
                channel = dto.channel
            )
        )

        // 3️⃣ Return domain model
        return CallModel(
            callId = dto.callId,
            status = CallStatus.INCOMING_RINGING,
            callType = callType,
            channel = dto.channel,
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId,
            calleeName = calleeName,
            calleeAvatar = calleeAvatar,
            rtcToken = null
        )
    }
}


