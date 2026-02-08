package com.example.app.core.device.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val api: DeviceApi
) {
    suspend fun sendFcmToken(sessionId: String, fcmToken: String): ApiResult<Unit> = safeApiCall {
        api.sendFcmToken(DeviceTokenRequest(sessionId, fcmToken))
    }
}
