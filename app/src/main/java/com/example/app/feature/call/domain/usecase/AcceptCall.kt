package com.example.app.feature.call.domain.usecase

import android.util.Log
import com.example.app.AppConstants
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.AcceptCallDto
import com.example.app.feature.call.data.CallRepository
import com.example.app.feature.call.domain.CallStatus
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
    ): ApiResult<AcceptCallDto> {

        val result = repo.acceptCall(callId)

        return when (result) {

            is ApiResult.Success -> {
                val dto = result.data
                val rtcChannel = dto.channel
                val rtcToken = dto.rtcToken

                Log.d("RTM", "acceptCall API → channel=$rtcChannel : rtcToken $rtcToken")

                // FAST PATH (notify caller)
                val payload = CallSignalPayload(
                    event = AppConstants.EVENT_CALL_ACCEPTED,
                    callId = callId,
                    callType = callType,
                    callerAccountId = callerAccountId,
                    calleeAccountId = calleeAccountId,
                    channel = rtcChannel,
                    rtcToken = rtcToken
                )

                rtmCallSignaling.sendCallEvent(
                    channel = RtmChannels.user(callerAccountId),
                    payload = payload
                )

                ApiResult.Success(
                    AcceptCallDto(
                        callId = callId,
                        status = CallStatus.CONNECTED,
                        channel = rtcChannel,
                        rtcToken = rtcToken
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

/*
class AcceptCall @Inject constructor(
    private val repo: CallRepository,
    private val rtmCallSignaling: RtmCallSignaling
) {

    suspend operator fun invoke(
        callId: String,
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long
    ): ApiResult<AcceptCallDto> {
        // 1️⃣ SLOW PATH — Backend source of truth
        // 1️⃣  First (get RTC channel) from Backend and persists call state
        val result = repo.acceptCall(callId)


        val rtcChannel = result.channel
        val rtcToken = result.rtcToken

        Log.d("RTM", "acceptCall API hit → channel=$rtcChannel : rtcToken $rtcToken")

        // 2️⃣ FAST PATH — Notify caller via RTM
        // RTM event drives UI + RTC join on BOTH devices
        val payload = CallSignalPayload(
            event = AppConstants.EVENT_CALL_ACCEPTED,
            callId = callId,
            callType = callType,
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId,
            channel = rtcChannel,
            rtcToken = rtcToken
        )

        // Send to caller
        rtmCallSignaling.sendCallEvent(
            channel = RtmChannels.user(callerAccountId),
            payload = payload
        )

        // ✅ RETURN DATA TO VIEWMODEL
        return AcceptCallDto(
            callId = callId,
            status = CallStatus.CONNECTED,
            channel = rtcChannel,
            rtcToken = rtcToken,
        )
    }
}
*/