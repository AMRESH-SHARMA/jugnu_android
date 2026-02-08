package com.example.app.core.device.data

import retrofit2.http.Body
import retrofit2.http.POST

data class DeviceTokenRequest(
    val sessionId: String,
    val fcmToken: String
)

interface DeviceApi {
    @POST("users/device-token")
    suspend fun sendFcmToken(@Body body: DeviceTokenRequest)
}
