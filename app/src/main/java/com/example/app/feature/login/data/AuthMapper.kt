package com.example.app.feature.login.data

import com.example.app.feature.login.domain.RequestOtpResult
import com.example.app.feature.login.domain.VerifyOtpResult


fun RequestOtpResponseDto.toDomain(): RequestOtpResult {
    return RequestOtpResult(
        message = this.message
    )
}

fun VerifyOtpResponseDto.toDomain(): VerifyOtpResult {
    return VerifyOtpResult(
        accessToken = this.accessToken,
        accountId = this.accountId,
        isNewUser = this.is_new_user
    )
}
