package com.example.app.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.MyApp
import com.example.app.core.session.SessionManager
import com.example.app.feature.navigation.ui.AppNavGraph

@Composable
fun AppRoot() {
    val vm: AppConfigViewModel = hiltViewModel()
    val state by vm.appConfig.collectAsState()
    
    val context = LocalContext.current
    val myApp = context.applicationContext as MyApp

    // 🚨 Listen globally for 426 events (backend forces update mid-session)
    LaunchedEffect(Unit) {
        ForceUpdateBus.events.collect {
            vm.setForceUpdate()
        }
    }
    
    // Initialize RTM after app config check passes
    LaunchedEffect(state.isLoading, state.forceUpdate) {
        if (!state.isLoading && !state.forceUpdate && SessionManager.userAccountId != 0L) {
            if (myApp.rtmInitialized.compareAndSet(false, true)) {
                android.util.Log.d("RTM", "AppRoot: Initializing RTM after config check")
                myApp.initAndLoginRtm(SessionManager.userAccountId)
            }
        }
    }

    when {
        state.isLoading -> SplashScreen()

        state.errorType == ErrorType.NO_INTERNET -> NoInternetScreen(
            onRetry = { vm.retry() }
        )

        state.errorType == ErrorType.SERVER_UNREACHABLE -> ServerUnreachableScreen(
            onRetry = { vm.retry() }
        )

        state.errorType == ErrorType.TIMEOUT -> TimeoutScreen(
            onRetry = { vm.retry() }
        )

        state.forceUpdate -> ForceUpdateScreen(
            message = state.forceMessage,
            playStoreUrl = state.playStoreUrl
        )

        // Only show AppNavGraph after session is fully loaded
        else -> AppNavGraph()
    }
}
