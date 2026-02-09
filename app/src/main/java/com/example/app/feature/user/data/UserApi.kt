package com.example.app.feature.user.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserApi {
    @GET("users/caller-info/{id}")
    suspend fun getCallerInfo(
        @Path("id") id: Long
    ): BaseResponse<CallerInfoDto>

    @PATCH("users/profile")
    suspend fun updateProfile(
        @Body req: UpdateProfileDto
    ): BaseResponse<Unit>

    @PATCH("users/availability")
    suspend fun updateAvailability(
        @Body req: UpdateAvailabilityDto
    ): BaseResponse<Unit>

    @retrofit2.http.POST("users/report-abuse")
    suspend fun reportAbuse(
        @Body req: ReportAbuseDto
    ): BaseResponse<Unit>

    @GET("users/recents")
    suspend fun getRecents(): BaseResponse<List<RecentInteractionDto>>
}
