package com.example.app.root

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.feature.navigation.ui.AppNavGraph
import com.example.app.feature.session.SessionViewModel
import com.example.app.MyApp

@Composable
fun AppRoot() {

    // -----------------------------
    // ViewModels
    // -----------------------------
    val configVm: AppConfigViewModel = hiltViewModel()
    val sessionVm: SessionViewModel = hiltViewModel()

    val configState by configVm.appConfig.collectAsState()
    val sessionState by sessionVm.session.sessionFlow.collectAsState()

    val accountId = sessionState.first

    val context = LocalContext.current
    val myApp = context.applicationContext as MyApp

    // -----------------------------
    // Force update global listener
    // -----------------------------
    LaunchedEffect(Unit) {
        ForceUpdateBus.events.collect {
            configVm.setForceUpdate()
        }
    }

    // -----------------------------
    // RTM Initialization
    // -----------------------------
    LaunchedEffect(
        configState.isLoading,
        configState.forceUpdate,
        accountId
    ) {
        if (!configState.isLoading &&
            !configState.forceUpdate &&
            accountId > 0
        ) {
            if (myApp.rtmInitialized.compareAndSet(false, true)) {

                Log.d(
                    "APP:ROOT",
                    "Initializing RTM for accountId=$accountId"
                )

                myApp.initAndLoginRtm(accountId)
            }
        }
    }

    // -----------------------------
    // UI Routing
    // -----------------------------
    when {
        configState.isLoading -> SplashScreen()

        configState.errorType == ErrorType.NO_INTERNET -> NoInternetScreen(
            onRetry = { configVm.retry() }
        )

        configState.errorType == ErrorType.SERVER_UNREACHABLE -> ServerUnreachableScreen(
            onRetry = { configVm.retry() }
        )

        configState.errorType == ErrorType.TIMEOUT -> TimeoutScreen(
            onRetry = { configVm.retry() }
        )

        configState.forceUpdate -> ForceUpdateScreen(
            message = configState.forceMessage,
            playStoreUrl = configState.playStoreUrl
        )

        else -> AppNavGraph()
    }
}