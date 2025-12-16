package com.example.app.feature.call.data

import com.example.app.core.call.CallType
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
    ): CallModel {

        val res = api.startCall(
            StartCallRequest(
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId,
                callType = callType
            )
        )

        if (!res.success) {
            throw Exception(res.message)
        }

        val data = res.data

        return CallModel(
            callId = data.callId,
            status = CallStatus.OUTGOING_RINGING,
            callType = callType,
            channel = data.channel,
            callerAccountId = callerAccountId,
            calleeAccountId = calleeAccountId,
        )
    }

    suspend fun acceptCall(callId: String): AcceptCallDto {
        val res = api.acceptCall(
            AcceptCallRequest(callId)
        )

        if (!res.success) {
            throw Exception(res.message)
        }

        return res.data
    }

    suspend fun rejectCall(callId: String): RejectCallDto {
        val res = api.rejectCall(
            RejectCallRequest(callId)
        )

        if (res.success) {
            return res.data
        }

        throw Exception(res.message)
    }

    suspend fun endCall(callId: String) {
        val res = api.endCall(
            EndCallRequest(callId)
        )

        if (!res.success) {
            throw Exception(res.message)
        }
    }
}
