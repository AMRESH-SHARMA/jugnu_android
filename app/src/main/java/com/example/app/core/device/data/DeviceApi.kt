package com.example.app.core.device.data

import retrofit2.http.Body
import retrofit2.http.POST

data class DeviceTokenRequest(
    val accountId: Long,
    val deviceToken: String
)

interface DeviceApi {
    @POST("users/device-token")
    suspend fun sendDeviceToken(@Body body: DeviceTokenRequest)
}
