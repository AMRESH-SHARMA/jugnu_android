package com.example.app.feature.login.data

import kotlinx.serialization.Serializable

//data class RequestOtpDto(
//    val phone: String
//)

data class VerifyOtpRequestDto(
    val phone: String,
    val otp: String
)

//data class RequestOtpResponseDto(
//    val success: Boolean,
//    val message: String
//)

data class VerifyOtpResponseDto(
    val token: String,
    val is_new_user: Boolean
)

// Request DTO
data class RequestOtpDto(val phone: String)

// Response DTO
data class RequestOtpResponseDto(
    val success: Boolean,
    val message: String
)