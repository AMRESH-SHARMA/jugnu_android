package com.example.app.core.remoteconfig

import android.util.Log
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
    val forceUpdate: Boolean,
    val offer: OfferConfig? = null
)

@Serializable
data class OfferConfig(
    val title: String,
    val body: String,
    val enabled: Boolean = true
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
                RemoteConfig.updateOffer(config.offer)

                Log.d("RTM CONFIG", "RemoteConfig updated → offer=${config.offer}")
                // persist
                repo.saveConfig(
                    apiBaseUrl = config.apiBaseUrl,
                    wsBaseUrl = config.wsBaseUrl
                )
            }
        } catch (e: Exception) {
            Log.e("RTM CONFIG", "❌ ConfigLoader.refresh FAILED", e)
        }
    }
}
