package com.example.app.feature.login.data

data class VerifyOtpRequestDto(
    val phone: String,
    val otp: String,
    val fcmToken: String?
)

data class VerifyOtpResponseDto(
    val accountId: Long,
    val sessionId: String,
    val userRole: String,
    val is_new_user: Boolean
)

// Request DTO
data class RequestOtpDto(val phone: String)

// Response DTO
data class RequestOtpResponseDto(
    val message: String
)