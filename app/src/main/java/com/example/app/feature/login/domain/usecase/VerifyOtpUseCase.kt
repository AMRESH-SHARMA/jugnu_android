package com.example.app.feature.login.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.login.data.AuthRepository
import com.example.app.feature.login.domain.VerifyOtpResult
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phone: String, otp: String): ApiResult<VerifyOtpResult> {
        return repository.verifyOtp(phone, otp)
    }
}