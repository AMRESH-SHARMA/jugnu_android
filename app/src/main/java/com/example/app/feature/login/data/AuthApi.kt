package com.example.app.feature.login.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/otp/request")
    suspend fun requestOtp(
        @Body req: RequestOtpDto
    ): BaseResponse<RequestOtpResponseDto>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(
        @Body req: VerifyOtpRequestDto
    ): BaseResponse<VerifyOtpResponseDto>
}