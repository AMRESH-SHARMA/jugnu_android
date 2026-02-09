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

    suspend fun updateProfile(nickname: String, interestedIn: String): ApiResult<Unit> = safeApiCall {
        val res = api.updateProfile(
            UpdateProfileDto(nickname, interestedIn)
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

    suspend fun getRecents(): ApiResult<List<RecentInteractionDto>> = safeApiCall {
        // TODO: Replace with actual API call when backend is ready
        // val res = api.getRecents()
        // if (!res.success) throw Exception(res.message)
        // res.data
        
        // Dummy data for demo
        kotlinx.coroutines.delay(800) // Simulate network delay
        
        listOf(
            RecentInteractionDto(
                listenerId = 1,
                listenerName = "Sarah Johnson",
                listenerAvatar = "https://i.pravatar.cc/150?img=47",
                lastInteractionType = "audio_call",
                lastInteractionTime = java.time.Instant.now().minus(5, java.time.temporal.ChronoUnit.MINUTES).toString(),
                interactionCount = 3
            ),
            RecentInteractionDto(
                listenerId = 2,
                listenerName = "Michael Chen",
                listenerAvatar = "https://i.pravatar.cc/150?img=12",
                lastInteractionType = "message",
                lastInteractionTime = java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.HOURS).toString(),
                interactionCount = 8
            ),
            RecentInteractionDto(
                listenerId = 3,
                listenerName = "Emma Williams",
                listenerAvatar = "https://i.pravatar.cc/150?img=45",
                lastInteractionType = "video_call",
                lastInteractionTime = java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS).toString(),
                interactionCount = 1
            ),
            RecentInteractionDto(
                listenerId = 4,
                listenerName = "David Martinez",
                listenerAvatar = "https://i.pravatar.cc/150?img=33",
                lastInteractionType = "audio_call",
                lastInteractionTime = java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS).toString(),
                interactionCount = 5
            ),
            RecentInteractionDto(
                listenerId = 5,
                listenerName = "Lisa Anderson",
                listenerAvatar = "https://i.pravatar.cc/150?img=26",
                lastInteractionType = "message",
                lastInteractionTime = java.time.Instant.now().minus(3, java.time.temporal.ChronoUnit.DAYS).toString(),
                interactionCount = 12
            ),
            RecentInteractionDto(
                listenerId = 6,
                listenerName = "James Wilson",
                listenerAvatar = "https://i.pravatar.cc/150?img=15",
                lastInteractionType = "video_call",
                lastInteractionTime = java.time.Instant.now().minus(5, java.time.temporal.ChronoUnit.DAYS).toString(),
                interactionCount = 2
            )
        )
    }
}
