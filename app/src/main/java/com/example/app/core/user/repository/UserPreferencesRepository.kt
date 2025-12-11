package com.example.app.core.user.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.app.core.user.domain.model.UserRole
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
    }

    val userPrefsFlow = dataStore.data.map { prefs ->
        Pair(
            prefs[KEY_ACCOUNT_ID] ?: "",
            UserRole.fromString(prefs[KEY_ROLE])
        )
    }

    suspend fun saveUserPrefs(accountId: String, role: UserRole) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_ID] = accountId
            prefs[KEY_ROLE] = role.name
        }
    }
}