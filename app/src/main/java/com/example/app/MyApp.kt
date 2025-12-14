package com.example.app

import android.app.Application
import android.util.Log
import com.example.app.core.device.TokenManager
import com.example.app.core.network.data.ApiRepository
import com.example.app.core.observer.EventObserver
import com.example.app.core.rtm.RtmEventListenerImpl
import com.example.app.core.rtm.RtmManager
import com.example.app.core.session.UserSession
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var apiRepository: ApiRepository

    private val rtmInitialized = AtomicBoolean(false)

    @Inject
    lateinit var eventObserver: EventObserver

    override fun onCreate() {
        super.onCreate()
        tokenManager.start()
        observeUserSession()
    }

    private fun observeUserSession() {
        CoroutineScope(Dispatchers.IO).launch {
            userSession.sessionFlow.collect { (accountId, _) ->
                if (accountId > 0 && rtmInitialized.compareAndSet(false, true)) {
                    initAndLoginRtm(accountId)
                }

            }
        }
    }

    private suspend fun initAndLoginRtm(accountId: Long) {
        try {
            val token = apiRepository.getRtmToken(accountId)

            Log.d("RTM", token)
            Log.d(
                "RTM",
                "App ID = '${BuildConfig.AGORA_APP_ID}' length=${BuildConfig.AGORA_APP_ID.length}"
            )

            // 1️⃣ INIT RTM (ONLY ONCE)
            RtmManager.init(
                context = applicationContext,
                appId = BuildConfig.AGORA_APP_ID,
                userId = accountId.toString(),
                listener = RtmEventListenerImpl(RtmManager.scope)
            )

            // 2️⃣ LOGIN RTM
            RtmManager.login(token)

        } catch (e: Exception) {
            // log / retry / backoff
        }
    }
}


