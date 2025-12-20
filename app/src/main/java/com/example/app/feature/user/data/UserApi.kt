package com.example.app.feature.user.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/caller-info/{id}")
    suspend fun getCallerInfo(
        @Path("id") id: Long
    ): BaseResponse<CallerInfoDto>
}

