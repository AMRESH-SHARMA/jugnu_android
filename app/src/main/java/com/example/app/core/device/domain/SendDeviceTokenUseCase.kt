package com.example.app.core.device.domain

import com.example.app.core.device.data.DeviceRepository
import javax.inject.Inject

class SendDeviceTokenUseCase @Inject constructor(
    private val repo: DeviceRepository
) {
    suspend operator fun invoke(accountId: Long, token: String) {
        repo.sendDeviceToken(accountId, token)
    }
}
