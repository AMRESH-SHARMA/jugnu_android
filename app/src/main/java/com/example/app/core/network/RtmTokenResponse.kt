package com.example.app.core.network

data class RtmTokenResponse(
    val data: TokenData
)

data class TokenData(
    val token: String
)
