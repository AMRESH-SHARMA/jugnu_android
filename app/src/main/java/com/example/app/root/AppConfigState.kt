package com.example.app.root

data class AppConfigState(
    val isLoading: Boolean = false,
    val forceUpdate: Boolean = false,
    val forceMessage: String? = null,
    val playStoreUrl: String? = null,
    val errorType: ErrorType? = null
)

enum class ErrorType {
    NO_INTERNET,
    SERVER_UNREACHABLE
}
