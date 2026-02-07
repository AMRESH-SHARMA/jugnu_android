package com.example.app.core.remoteconfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class RemoteConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
        val KEY_WS_BASE_URL = stringPreferencesKey("ws_base_url")
        val KEY_CONFIG_TIMESTAMP = longPreferencesKey("config_timestamp")

        private const val CONFIG_TTL_MS = 3600_000L // 1 hour
    }

    suspend fun shouldRefreshConfig(): Boolean {
        val lastFetch = dataStore.data.first()[KEY_CONFIG_TIMESTAMP] ?: 0L
        val now = System.currentTimeMillis()
        return (now - lastFetch) > CONFIG_TTL_MS
    }

    suspend fun saveConfig(
        apiBaseUrl: String,
        wsBaseUrl: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_API_BASE_URL] = apiBaseUrl
            prefs[KEY_WS_BASE_URL] = wsBaseUrl
            prefs[KEY_CONFIG_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun loadCachedConfig(): Pair<String?, String?> {
        val prefs = dataStore.data.first()
        return Pair(
            prefs[KEY_API_BASE_URL],
            prefs[KEY_WS_BASE_URL]
        )
    }
}

/*
@Singleton
class RemoteConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
        private val KEY_WS_BASE_URL = stringPreferencesKey("ws_base_url")
    }

    suspend fun saveApiBaseUrl(url: String) {
        dataStore.edit { prefs ->
            prefs[KEY_API_BASE_URL] = url
        }
    }

    suspend fun loadApiBaseUrl(): String? {
        return dataStore.data.first()[KEY_API_BASE_URL]
    }

    suspend fun saveWsBaseUrl(url: String?) {
        dataStore.edit { prefs ->
            if (url == null) prefs.remove(KEY_WS_BASE_URL)
            else prefs[KEY_WS_BASE_URL] = url
        }
    }

    suspend fun loadWsBaseUrl(): String? {
        return dataStore.data.first()[KEY_WS_BASE_URL]
    }
}


 */