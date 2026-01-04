package com.example.app.feature.login.data

import com.example.app.feature.login.domain.RequestOtpResult
import com.example.app.feature.login.domain.VerifyOtpResult


fun RequestOtpResponseDto.toDomain(): RequestOtpResult {
    return RequestOtpResult(
        success = this.success,
        message = this.message
    )
}

fun VerifyOtpResponseDto.toDomain(): VerifyOtpResult {
    return VerifyOtpResult(
        token = this.token,
        isNewUser = this.is_new_user
    )
}
