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

//class UserRepository @Inject constructor(
//    private val api: UserApi
//) {
//
//    private val callerInfoCache = mutableMapOf<Long, CallerInfo>()
//
//    suspend fun getCallerInfo(userId: Long): ApiResult<CallerInfo> {
//
//        // 1️⃣ Return from cache if exists
//        callerInfoCache[userId]?.let { cached ->
//            return ApiResult.Success(cached)
//        }
//
//        // 2️⃣ Fetch from network
//        return try {
//            val res = api.getCallerInfo(userId)
//
//            if (res.success) {
//                // 3️⃣ Save to cache
//                val callerInfo = res.data.toDomain()
//                callerInfoCache[userId] = callerInfo
//
//                ApiResult.Success(res.data.toDomain())
//            } else {
//                ApiResult.Error(res.message)
//            }
//
//        } catch (e: Exception) {
//            ApiResult.Error(
//                message = e.localizedMessage,
//                exception = e
//            )
//        }
//    }
//}
