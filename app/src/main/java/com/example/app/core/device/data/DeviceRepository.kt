package com.example.app.core.device.data

import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val api: DeviceApi
) {

    suspend fun sendDeviceToken(userId: String, token: String) {
        api.sendDeviceToken(DeviceTokenRequest(userId, token))
    }
}
