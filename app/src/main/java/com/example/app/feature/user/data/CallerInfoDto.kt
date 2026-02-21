package com.example.app.feature.user.data

data class CallerInfoDto(
    val name: String,
    val avatar: String?
)
data class CustomerProfileDto(
    val name: String,
    val balanceCoins: Long,
    val isProfileComplete: Boolean = false
)
