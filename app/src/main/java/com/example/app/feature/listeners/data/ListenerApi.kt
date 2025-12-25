package com.example.app.feature.listeners.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ListenerApi {
    @GET("users/listeners")
    suspend fun getListeners(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseResponse<List<ListenerDto>>
//    @GET("users/listeners")
//    suspend fun getListeners(): BaseResponse<List<ListenerDto>>

}

