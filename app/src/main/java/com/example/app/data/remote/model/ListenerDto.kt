package com.example.app.data.remote.model

data class ListenerDto(
    val id: Int,
    val userId: Int,
    val avatar: String?,
    val bio: String?,
    val about: String?,
    val experience: Int,
    val pricePerMin: Double?,
    val languages: String?,
    val rating: Double?,
)
