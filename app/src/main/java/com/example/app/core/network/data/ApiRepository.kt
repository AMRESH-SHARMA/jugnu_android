package com.example.app.core.network.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val authApi: RtmAuthApi,
    private val callNotificationApi: CallNotificationApi
) {
    suspend fun getRtmToken(accountId: Long): ApiResult<String> = safeApiCall {
        val response = authApi.getRtmToken(mapOf("accountId" to accountId))
//        Log.w("RTM", "TOKEN $response")
        response.data.token
    }


    // TODO
    suspend fun notifyCallViaFcm(
        callId: String,
        callerId: Long,
        calleeId: Long
    ): ApiResult<Unit> = safeApiCall {
        callNotificationApi.notifyCallViaFcm(
            NotifyFcmCallRequest(callId, callerId, calleeId)
        )
    }
}