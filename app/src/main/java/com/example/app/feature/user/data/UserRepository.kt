package com.example.app.feature.user.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.BaseResponse
import com.example.app.core.network.safeApiCall
import com.example.app.feature.user.domain.CallerInfo
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getCallerInfo(userId: Long): ApiResult<CallerInfo> = safeApiCall {
        val res = BaseResponse(
            success = true,
            message = "User lite profile fetched",
            data = CallerInfoDto(
                name = "Anonym",
                avatar = "https://i.pravatar.cc/150?img=48"
            )
        )
//        val res2 = api.getCallerInfo(userId)
        if (!res.success) throw Exception(res.message)
        res.data.toDomain()
    }

    suspend fun updateProfile(nickname: String, gender: String? = null, interestedIn: String): ApiResult<Unit> = safeApiCall {
        val res = api.updateProfile(
            UpdateProfileDto(nickname, gender, interestedIn)
        )
        if (!res.success) throw Exception(res.message)
    }

    suspend fun updateAvailability(isAvailable: Boolean): ApiResult<Unit> = safeApiCall {
        val res = api.updateAvailability(
            UpdateAvailabilityDto(isAvailable)
        )
        if (!res.success) throw Exception(res.message)
    }

    suspend fun reportAbuse(description: String): ApiResult<Unit> = safeApiCall {
        val res = api.reportAbuse(
            ReportAbuseDto(description)
        )
        if (!res.success) throw Exception(res.message)
    }

    suspend fun logout(): ApiResult<Unit> = safeApiCall {
        val res = api.logout()
        if (!res.success) throw Exception(res.message)
    }

    suspend fun getRecents(): ApiResult<List<RecentInteractionDto>> = safeApiCall {
        val res = api.getRecents()
        if (!res.success) throw Exception(res.message)
        res.data
    }

    suspend fun getCustomerProfile(): ApiResult<CustomerProfileDto> = safeApiCall {
        val res = api.getCustomerProfile()
        if (!res.success) throw Exception(res.message)
        res.data
    }
}
