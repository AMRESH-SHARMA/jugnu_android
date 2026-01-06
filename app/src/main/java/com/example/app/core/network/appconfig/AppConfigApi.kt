package com.example.app.core.network.appconfig

import retrofit2.http.GET

interface AppConfigApi {
    @GET("/app-config")
    suspend fun getConfig(): AppConfigResponse
}

data class AppConfigResponse(
    val min_supported_version: Int,
    val latest_version: Int,
    val force_message: String?,
    val play_store_url: String?
)
