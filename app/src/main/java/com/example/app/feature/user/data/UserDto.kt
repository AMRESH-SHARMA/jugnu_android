package com.example.app.feature.user.data

data class UpdateProfileDto(
    val nickname: String,
    val gender: String? = null,
    val interestedIn: String
)
data class UpdateAvailabilityDto(
    val isAvailable: Boolean
)

data class ReportAbuseDto(
    val description: String
)

data class RecentInteractionDto(
    val listenerId: Long,
    val listenerName: String,
    val listenerAvatar: String?,
    val lastInteractionType: String, // "audio_call", "video_call", "message"
    val lastInteractionTime: String, // ISO timestamp
    val interactionCount: Int
)
