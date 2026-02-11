package com.example.app.core.network

data class ApiErrorResponse(
    val success: Boolean,
    val message: String,
    val meta: Map<String, Any>? = null
)