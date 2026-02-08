package com.example.app.core.preferences.user.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.app.core.preferences.user.domain.UserRole
import kotlinx.coroutines.flow.first
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
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_SESSION_ID = stringPreferencesKey("session_id")


        // 🆕 Offer tracking (single offer, content-based)
        val KEY_LAST_OFFER_DATE = stringPreferencesKey("last_offer_date")
        val KEY_LAST_OFFER_SIGNATURE = stringPreferencesKey("last_offer_signature")
    }

    val userPrefsFlow = dataStore.data.map { prefs ->
        Pair(
            prefs[KEY_ACCOUNT_ID]?.toLongOrNull() ?: 0L,
            UserRole.Companion.fromString(prefs[KEY_ROLE])
        )
    }

    val fcmTokenFlow = dataStore.data.map { prefs ->
        prefs[KEY_FCM_TOKEN]
    }

    val sessionIdFlow = dataStore.data.map { prefs ->
        prefs[KEY_SESSION_ID] ?: ""
    }

    suspend fun saveUserPrefs(id: Long, role: UserRole) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_ID] = id.toString()
            prefs[KEY_ROLE] = role.name
        }
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }

    suspend fun saveSessionId(sessionId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = sessionId
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_SESSION_ID)
            prefs.remove(KEY_ACCOUNT_ID)
            prefs.remove(KEY_ROLE)
        }
    }

    // ---------------------------------------------------------
    // 🆕 Advertisement/Offer helpers
    // ---------------------------------------------------------

    suspend fun getLastOfferShownDate(): String? {
        return dataStore.data.first()[KEY_LAST_OFFER_DATE]
    }

    suspend fun getLastOfferSignature(): String? {
        return dataStore.data.first()[KEY_LAST_OFFER_SIGNATURE]
    }

    /**
     * Call this when the offer dialog is dismissed
     */
    suspend fun markOfferShown(
        signature: String,
        date: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_OFFER_SIGNATURE] = signature
            prefs[KEY_LAST_OFFER_DATE] = date
        }
    }
}