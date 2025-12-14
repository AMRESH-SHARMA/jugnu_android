package com.example.app.feature.call.data

import javax.inject.Inject

class CallRepository @Inject constructor(
    private val api: CallApi
) {

    suspend fun startCall(
        callerAccountId: Long,
        calleeAccountId: Long
    ): StartCallDto {
        val res = api.startCall(
            StartCallRequest(
                callerAccountId = callerAccountId,
                calleeAccountId = calleeAccountId
            )
        )

        if (res.success) {
            return res.data
        }

        throw Exception(res.message)
    }

    suspend fun acceptCall(callId: String): AcceptCallDto {
        val res = api.acceptCall(
            AcceptCallRequest(callId)
        )

        if (res.success) {
            return res.data
        }

        throw Exception(res.message)
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
