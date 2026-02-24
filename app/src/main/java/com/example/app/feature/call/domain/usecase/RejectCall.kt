package com.example.app.feature.call.domain.usecase

import com.example.app.AppConstants
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.rtm.RtmCallSignaling
import com.example.app.core.rtm.RtmChannels
import com.example.app.feature.call.data.CallRepository
import javax.inject.Inject

class RejectCall @Inject constructor(
    private val repo: CallRepository,
) {
    suspend operator fun invoke(
        callId: String,
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long
    ): ApiResult<Unit> {

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