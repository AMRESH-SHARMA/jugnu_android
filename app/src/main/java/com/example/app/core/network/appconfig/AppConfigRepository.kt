package com.example.app.core.network.appconfig

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import javax.inject.Inject

class AppConfigRepository @Inject constructor(
    private val api: AppConfigApi
) {
    suspend fun fetchConfig(): ApiResult<AppConfigResponse> =
        safeApiCall { api.getConfig() }
}
