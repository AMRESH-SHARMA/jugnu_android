package com.example.app.core.preferences.user.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.app.core.preferences.user.domain.UserRole
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val KEY_ACCOUNT_ID = stringPreferencesKey("account_id")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    val userPrefsFlow = dataStore.data.map { prefs ->
        Pair(
            prefs[KEY_ACCOUNT_ID]?.toLongOrNull() ?: 0L,
            UserRole.Companion.fromString(prefs[KEY_ROLE])
        )
    }

    val tokenFlow = dataStore.data.map { prefs ->
        prefs[KEY_FCM_TOKEN]
    }

    suspend fun saveUserPrefs(id: Long, role: UserRole) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_ID] = id.toString()
            prefs[KEY_ROLE] = role.name
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }
}