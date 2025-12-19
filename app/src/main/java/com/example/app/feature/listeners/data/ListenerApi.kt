package com.example.app.feature.listeners.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET

interface ListenerApi {
    @GET("users/listeners")
    suspend fun getListeners(): BaseResponse<List<ListenerDto>>
}

