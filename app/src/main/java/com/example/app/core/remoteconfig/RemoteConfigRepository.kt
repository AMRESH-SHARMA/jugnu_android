package com.example.app.core.remoteconfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

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
