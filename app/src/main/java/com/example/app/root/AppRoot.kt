package com.example.app.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.feature.navigation.ui.AppNavGraph

@Composable
fun AppRoot() {
    val vm: AppConfigViewModel = hiltViewModel()
    val state by vm.appConfig.collectAsState()

    // 🚨 Listen globally for 426 events (backend forces update mid-session)
    LaunchedEffect(Unit) {
        ForceUpdateBus.events.collect {
            vm.setForceUpdate()
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

        state.forceUpdate -> ForceUpdateScreen(
            message = state.forceMessage,
            playStoreUrl = state.playStoreUrl
        )

        else -> AppNavGraph()
    }
}
