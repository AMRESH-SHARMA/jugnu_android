package com.example.app.feature.user.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.user.domain.CallerInfo
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getCallerInfo(userId: Long): ApiResult<CallerInfo> = safeApiCall {
        val res = api.getCallerInfo(userId)

        if (!res.success) throw Exception(res.message)

        res.data.toDomain()
    }
}