package com.example.app.core.network.data


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val authApi: RtmAuthApi,
    private val callNotificationApi: CallNotificationApi
) {

    suspend fun getRtmToken(accountId: Long): String {
        val response = authApi.getRtmToken(mapOf("accountId" to accountId))
        return response.data.token
    }

    suspend fun notifyCallViaFcm(callId: String, callerId: Long, calleeId: Long) {
        callNotificationApi.notifyCallViaFcm(
            NotifyFcmCallRequest(callId, callerId, calleeId)
        )
    }
}



