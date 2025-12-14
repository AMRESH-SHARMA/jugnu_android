package com.example.app.core.device.data

import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val api: DeviceApi
) {
    suspend fun sendDeviceToken(accountId: Long, token: String) {
        api.sendDeviceToken(DeviceTokenRequest(accountId, token))
    }
}
