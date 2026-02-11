package com.example.app.core.network

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {
    
    fun getErrorMessage(exception: Throwable, backendMessage: String? = null): String {
        return when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    401, 403 -> "Session expired. Please login again"
                    402 -> backendMessage ?: "Insufficient balance to make this call"
                    404 -> backendMessage ?: "Resource not found"
                    408 -> "Request timeout. Please try again"
                    500 -> "Server error. Please try again later"
                    502 -> "Bad gateway. Please try again"
                    503 -> "Service unavailable. Please try again later"
                    504 -> "Gateway timeout. Please try again"
                    else -> backendMessage ?: "Something went wrong"
                }
            }
            is SocketTimeoutException -> "Request timeout. Check your connection"
            is UnknownHostException -> "No internet connection"
            is IOException -> "Network error. Check your connection"
            else -> exception.localizedMessage ?: "Unknown error occurred"
        }
    }
    
    fun isSessionExpired(exception: Throwable): Boolean {
        return exception is HttpException && (exception.code() == 401 || exception.code() == 403)
    }
    
    fun isInsufficientBalance(exception: Throwable): Boolean {
        return exception is HttpException && exception.code() == 402
    }
}
