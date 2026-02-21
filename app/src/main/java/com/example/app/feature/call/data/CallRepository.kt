package com.example.app.feature.call.data

import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import javax.inject.Inject

class CallRepository @Inject constructor(
    private val api: CallApi
) {
    suspend fun startCall(
        callerAccountId: Long,
        calleeAccountId: Long,
        callType: CallType
    ): ApiResult<CallModel> = safeApiCall {
        val res = api.startCall(
            StartCallRequest(
                callerAccountId,
                calleeAccountId,
                callType
            )
        )

        if (!res.success) throw Exception(res.message)

        val data = res.data

        CallModel(
            callId = data.callId,
            status = CallStatus.OUTGOING_CONNECTING,
            callType = callType,
            channel = data.channel,
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId
        )
    }

    suspend fun acceptCall(callId: String): ApiResult<AcceptCallDto> = safeApiCall {
        val res = api.acceptCall(AcceptCallRequest(callId))
        if (!res.success) throw Exception(res.message)
        res.data
    }

    suspend fun rejectCall(callId: String): ApiResult<RejectCallDto> = safeApiCall {
        val res = api.rejectCall(RejectCallRequest(callId))
        if (!res.success) throw Exception(res.message)
        res.data
    }

    suspend fun cancelCall(callId: String): ApiResult<Unit> = safeApiCall {
        val res = api.cancelCall(CancelCallRequest(callId))
        if (!res.success) throw Exception(res.message)
    }

    suspend fun endCall(callId: String): ApiResult<Unit> = safeApiCall {
        val res = api.endCall(EndCallRequest(callId))
        if (!res.success) throw Exception(res.message)
    }

    suspend fun callReceived(callId: String, calleeAccountId: Long): ApiResult<Unit> = safeApiCall {
        val res = api.callReceived(CallReceivedRequest(callId, calleeAccountId))
        if (!res.success) throw Exception(res.message)
    }
}