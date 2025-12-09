package com.example.app.feature.listeners.data

data class ListenerDto(
    val accountId: Long,
    val name: String,
    val age: Int,
    val gender: String,
    val avatar: String?,
    val tagLine: String?,
    val about: String?,
    val experience: Int,
    val pricePerMin: Double,
    val languages: List<String>,
    val rating: Double
)