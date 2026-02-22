package com.example.app.feature.login.data


import android.util.Log
import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.login.domain.RequestOtpResult
import com.example.app.feature.login.domain.VerifyOtpResult
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi
) {

    // ------------------- Request OTP -------------------
    suspend fun requestOtp(phone: String): ApiResult<RequestOtpResult> = safeApiCall {
        val res = api.requestOtp(RequestOtpDto(phone))
        if (!res.success) throw Exception(res.message)

        // Map DTO to Domain
        res.data?.toDomain() ?: throw IllegalArgumentException("OTP response data is null")
    }

    // ------------------- Verify OTP -------------------
    suspend fun verifyOtp(
        phone: String,
        otp: String,
        fcmToken: String?
    ): ApiResult<VerifyOtpResult> =
        safeApiCall {
            val res = api.verifyOtp(
                VerifyOtpRequestDto(phone, otp, fcmToken)
            )
            if (!res.success) throw Exception(res.message)
            res.data?.toDomain()
                ?: throw IllegalArgumentException("Verify OTP response data is null")
        }
}
