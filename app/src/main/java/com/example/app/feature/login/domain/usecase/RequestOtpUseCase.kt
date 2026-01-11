package com.example.app.feature.login.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.login.data.AuthRepository
import com.example.app.feature.login.domain.RequestOtpResult
import javax.inject.Inject

class RequestOtpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phone: String): ApiResult<RequestOtpResult> {
        return repository.requestOtp(phone)
    }
}
