package com.example.app.core.network

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)