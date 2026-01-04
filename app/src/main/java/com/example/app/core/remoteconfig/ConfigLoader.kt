package com.example.app.core.remoteconfig

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class AppConfig(
    val apiBaseUrl: String,
    val wsBaseUrl: String,
    val cdnBaseUrl: String,
    val minVersion: Int,
    val forceUpdate: Boolean
)

object ConfigLoader {

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun refresh(repo: RemoteConfigRepository) {
        try {
            val request = Request.Builder()
                .url(RemoteConfig.REMOTE_CONFIG_URL)
                .build()

            client.newCall(request).execute().use { res ->
                val body = res.body?.string() ?: return
                val config = json.decodeFromString<AppConfig>(body)

                // update memory
                RemoteConfig.updateApi(config.apiBaseUrl)
                RemoteConfig.updateWs(config.wsBaseUrl)

                // persist
                repo.saveApiBaseUrl(config.apiBaseUrl)
                repo.saveWsBaseUrl(config.wsBaseUrl)
            }
        } catch (_: Exception) {
        }
    }

}
