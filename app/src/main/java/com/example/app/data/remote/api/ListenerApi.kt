package com.example.app.data.remote.api

import com.example.app.data.remote.model.ListenerDto
import retrofit2.http.GET

interface ListenerApi {

    @GET("api/v1/listeners")
    suspend fun getListeners(
    ): ListenerResponse
}

data class ListenerResponse(
    val success: Boolean,
    val message: String,
    val data: List<ListenerDto>
)

