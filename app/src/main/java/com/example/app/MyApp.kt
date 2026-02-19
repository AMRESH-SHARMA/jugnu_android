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
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import com.example.app.core.session.SessionManager

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

    private val rtmInitialized = AtomicBoolean(false)

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

        // 2️⃣ Now it’s safe to start the rest
        tokenManager.start()
        restoreSessionId()
        observeUserSession()
        eventObserver.toString()
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
        
        Log.d("Crashlytics", "Firebase Crashlytics initialized")
    }

    //Restore from DATA Store and put in SessionManager
    private fun restoreSessionId() {
        appScope.launch(Dispatchers.IO) {
            SessionManager.sessionId = userSession.sessionId
        }
    }


    private fun observeUserSession() {
        Log.d("RTM", "MyApp: Starting to observe user session flow")
        appScope.launch(Dispatchers.IO) {
            combine(
                userSession.sessionFlow,
                userSession.sessionIdFlow
            ) { (accountId, role), sessionId ->
                Triple(accountId, role, sessionId)
            }.collect { (accountId, role, sessionId) ->
                val isLoggedIn = accountId > 0 && !sessionId.isNullOrBlank()
                
                Log.d("RTM", "MyApp: Session state - accountId=$accountId, role=$role, sessionId=$sessionId, isLoggedIn=$isLoggedIn")
                
                if (isLoggedIn) {
                    Log.d("RTM", "MyApp: Valid session detected → connecting WebSocket")
                    presenceWebSocketManager.connect()

                    if (rtmInitialized.compareAndSet(false, true)) {
                        Log.d("RTM", "MyApp: Initializing RTM for first time")
                        initAndLoginRtm(accountId)
                    } else {
                        Log.d("RTM", "MyApp: RTM already initialized, skipping")
                    }
                } else {
                    Log.d("RTM", "MyApp: No valid session (accountId=$accountId, sessionId=$sessionId) → disconnecting WebSocket")
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


