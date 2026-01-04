package com.example.app.root

data class AppConfigState(
    val isLoading: Boolean = false,
    val forceUpdate: Boolean = false,
    val forceMessage: String? = null,
    val playStoreUrl: String? = null
)
