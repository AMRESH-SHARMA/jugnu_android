package com.example.app.feature.call.data

import com.example.app.feature.call.domain.CallModel
import javax.inject.Inject

class CallRepository @Inject constructor(
    private val api: CallApi
) {

    suspend fun startCall(callerId: String, calleeId: String, channel: String): CallModel {
        val res = api.startCall(StartCallRequest(callerId, calleeId, channel))

        if (res.success)
            return CallModel(res.data.callId, res.data.status, channel)

        throw Exception(res.message)
    }

    suspend fun acceptCall(callId: String): CallModel {
        val res = api.acceptCall(AcceptCallRequest(callId))

        if (res.success)
            return CallModel(res.data.callId, res.data.status, res.data.channel)

        throw Exception(res.message)
    }

    suspend fun rejectCall(callId: String): CallModel {
        val res = api.rejectCall(RejectCallRequest(callId))

        if (res.success)
            return CallModel(res.data.callId, res.data.status)

        throw Exception(res.message)
    }
}
