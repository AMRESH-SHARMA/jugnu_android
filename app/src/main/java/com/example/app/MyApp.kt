package com.example.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.app.core.device.TokenManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.network.ApiResult
import com.example.app.core.network.data.ApiRepository
import com.example.app.core.observer.AppForegroundTracker
import com.example.app.core.observer.EventObserver
import com.example.app.core.remoteconfig.ConfigLoader
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.core.remoteconfig.RemoteConfigRepository
import com.example.app.core.rtm.RtmEventListenerImpl
import com.example.app.core.rtm.RtmManager
import com.example.app.core.session.UserSession
import com.example.app.core.websocket.PresenceWebSocketManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import com.example.app.core.session.SessionManager
import kotlinx.coroutines.flow.distinctUntilChanged

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var appForegroundTracker: AppForegroundTracker

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

    val rtmInitialized = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase Crashlytics
        initializeCrashlytics()
        
        /** Observe whether app is in foreground/background */
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(appForegroundTracker)

        appScope.launch(Dispatchers.IO) {
            if (AppConstants.USE_DEFAULT_URL) {
                RemoteConfig.updateApi(AppConstants.DEFAULT_BASE_URL)
            } else {
                // Load cached config immediately
                val (cachedApi, cachedWs) = remoteConfigRepo.loadCachedConfig()
                if (cachedApi != null) RemoteConfig.updateApi(cachedApi)
                if (cachedWs != null) RemoteConfig.updateWs(cachedWs)

                // Refresh only if TTL expired
                if (remoteConfigRepo.shouldRefreshConfig()) {
                    ConfigLoader.refresh(remoteConfigRepo)
                }
            }
        }

        // Wait for valid session to load, then start services
        appScope.launch(Dispatchers.IO) {
            // Wait for VALID session (not just first emission of default values)
            val (accountId, sessionId) = combine(
                userSession.sessionFlow,
                userSession.sessionIdFlow
            ) { (accountId, _), sessionId ->
                Triple(accountId, sessionId, accountId > 0 && sessionId.isNotBlank())
            }.first { it.third }
                .let { Pair(it.first, it.second) }

            Log.d("APP", "MyApp: Valid session loaded - accountId=$accountId, sessionId=$sessionId")

            // Start services (but NOT RTM - moved to AppRoot after config check)
            tokenManager.start()
            eventObserver.start()
            observeUserSession() // Only for WebSocket, not RTM
        }
    }
    
    private fun initializeCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Enable crash reporting
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Set user identifier (will be updated when user logs in)
        if (SessionManager.userAccountId != 0L) {
            crashlytics.setUserId(SessionManager.userAccountId.toString())
        }
        
        // Add custom keys for debugging
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        
        Log.d("APP", "Firebase Crashlytics initialized")
    }

    // Tracks future session changes like logout.
    private fun observeUserSession() {
        Log.d("APP", "MyApp: Starting to observe user session flow (WebSocket only)")
        appScope.launch(Dispatchers.IO) {
            combine(
                userSession.sessionFlow,
                userSession.sessionIdFlow
            ) { (accountId, role), sessionId ->
                Triple(accountId, role, sessionId)
            }
            .distinctUntilChanged()
            .collect { (accountId, role, sessionId) ->
                val isLoggedIn = accountId > 0 && !sessionId.isNullOrBlank()
                
                Log.d("APP", "MyApp: Session state - accountId=$accountId, role=$role, sessionId=$sessionId, isLoggedIn=$isLoggedIn")
                
                if (isLoggedIn) {
                    Log.d("APP", "MyApp: Valid session detected → connecting WebSocket")
                    presenceWebSocketManager.connect()
                } else {
                    Log.d("APP", "MyApp: No valid session → disconnecting WebSocket")
                    presenceWebSocketManager.disconnect()
                }
            }
        }
    }

    // RTM initialization moved to AppRoot (after app config check)
    suspend fun initAndLoginRtm(accountId: Long) {

        when (val result = apiRepository.getRtmToken(accountId)) {

            is ApiResult.Success -> {
                val token = result.data
                Log.d(
                    "APP",
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
                Log.e("APP", "Failed to get RTM token: ${result.message}")
                // optional: retry/backoff or PresenceEventBus
            }
        }
    }
}
