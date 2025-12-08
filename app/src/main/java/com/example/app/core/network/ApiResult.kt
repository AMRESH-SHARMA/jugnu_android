package com.example.app.core.network

sealed class ApiResult<out T> {

    data class Success<out T>(val data: T) : ApiResult<T>()

    data class Error(
        val message: String? = null,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()
}
