package com.example.app

import android.app.Application
import android.util.Log
import com.example.app.core.device.TokenManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.network.ApiResult
import com.example.app.core.network.data.ApiRepository
import com.example.app.core.observer.EventObserver
import com.example.app.core.remoteconfig.ConfigLoader
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.core.remoteconfig.RemoteConfigRepository
import com.example.app.core.rtm.RtmEventListenerImpl
import com.example.app.core.rtm.RtmManager
import com.example.app.core.session.UserSession
import com.example.app.core.websocket.PresenceWebSocketManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var remoteConfigRepo: RemoteConfigRepository

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var apiRepository: ApiRepository


    @Inject
    lateinit var eventObserver: EventObserver

    @Inject
    lateinit var presenceWebSocketManager: PresenceWebSocketManager

    private val rtmInitialized = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        appScope.launch(Dispatchers.IO) {

            // 1️⃣ load cached value (fast)
            val cached = remoteConfigRepo.loadApiBaseUrl()
            if (cached != null) RemoteConfig.updateApi(cached)

            // 2️⃣ fetch GitHub config (background)
            ConfigLoader.refresh(remoteConfigRepo)
        }

        // 2️⃣ Now it’s safe to start the rest
        tokenManager.start()
        observeUserSession()
        eventObserver.toString()
    }

    private fun observeUserSession() {
        appScope.launch(Dispatchers.IO) {
            userSession.sessionFlow.collect { (accountId, _) ->
                if (accountId > 0) {
                    // 🔥 THIS is what you were missing
                    presenceWebSocketManager.connect()

                    if (rtmInitialized.compareAndSet(false, true)) {
                        initAndLoginRtm(accountId)
                    }
                } else {
                    presenceWebSocketManager.disconnect()
                }

            }
        }
    }

    private suspend fun initAndLoginRtm(accountId: Long) {

        when (val result = apiRepository.getRtmToken(accountId)) {

            is ApiResult.Success -> {
                val token = result.data
                Log.d(
                    "RTM",
                    "App ID='${BuildConfig.AGORA_APP_ID}' length=${BuildConfig.AGORA_APP_ID.length} token=$token"
                )

                // INIT RTM once
                RtmManager.init(
                    context = applicationContext,
                    appId = BuildConfig.AGORA_APP_ID,
                    userId = accountId.toString(),
                    listener = RtmEventListenerImpl(appScope),
                    appScope = appScope
                )

                // LOGIN RTM
                RtmManager.login(token)
            }

            is ApiResult.Error -> {
                Log.e("RTM", "Failed to get RTM token: ${result.message}")
                // optional: retry/backoff or PresenceEventBus
            }
        }
    }
}


