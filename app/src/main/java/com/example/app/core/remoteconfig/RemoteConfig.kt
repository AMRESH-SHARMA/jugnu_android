package com.example.app.core.remoteconfig

import com.example.app.utils.AppConstants

object RemoteConfig {

    const val DEFAULT_BASE_URL = AppConstants.DEFAULT_BASE_URL
    const val REMOTE_CONFIG_URL = AppConstants.REMOTE_CONFIG_URL

    @Volatile
    var apiBaseUrl: String = DEFAULT_BASE_URL
        private set

    @Volatile
    var wsBaseUrl: String? = null

    fun updateApi(url: String) {
        apiBaseUrl = url
    }

    fun updateWs(url: String?) {
        wsBaseUrl = url
    }
}
