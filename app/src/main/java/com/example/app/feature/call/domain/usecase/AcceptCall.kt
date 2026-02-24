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

                Log.d("APP:CALLAPI", "acceptCall API → channel=$rtcChannel : rtcToken $rtcToken")

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