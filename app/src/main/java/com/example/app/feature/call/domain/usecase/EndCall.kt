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
    private val repo: CallRepository
) {

    suspend operator fun invoke(
        callId: String
    ): ApiResult<Unit> {

        Log.d("APP:CALLAPI", "EndCall → callId=$callId")

        return when (val result = repo.endCall(callId)) {
            is ApiResult.Success -> ApiResult.Success(Unit)

            is ApiResult.Error -> ApiResult.Error(
                message = result.message,
                code = result.code,
                exception = result.exception
            )
        }
    }
}