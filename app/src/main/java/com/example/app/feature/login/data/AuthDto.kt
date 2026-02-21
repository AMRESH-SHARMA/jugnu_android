package com.example.app.feature.login.data

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequestDto(
    val phone: String,
    val otp: String,
    val fcmToken: String?
)

data class VerifyOtpResponseDto(
    val accountId: Long,
    val sessionId: String,
    val userRole: String,
    @SerializedName("is_new_user", alternate = ["isNewUser"])
    val is_new_user: Boolean,
    @SerializedName("is_profile_complete", alternate = ["isProfileComplete"])
    val is_profile_complete: Boolean = false
)

// Request DTO
data class RequestOtpDto(val phone: String)

// Response DTO
data class RequestOtpResponseDto(
    val message: String
)