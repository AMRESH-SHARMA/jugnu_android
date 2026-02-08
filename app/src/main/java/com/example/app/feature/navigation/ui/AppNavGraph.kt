package com.example.app.feature.navigation.ui

import Routes
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.core.call.CallStore
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.core.session.SessionManager
import com.example.app.core.ui.UiEvent
import com.example.app.core.ui.UiEventBus
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.call.ui.IncomingCallBanner
import kotlinx.coroutines.flow.drop

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    // Observe call state for UI overlays
    val callState by CallStore.call.collectAsState(initial = null)

    // Disable back button during incoming or ongoing calls
    androidx.activity.compose.BackHandler(
        enabled = callState?.status == CallStatus.INCOMING_RINGING ||
                  callState?.status == CallStatus.OUTGOING_RINGING ||
                  callState?.status == CallStatus.CONNECTING ||
                  callState?.status == CallStatus.CONNECTED
    ) {
        // Do nothing - prevent back navigation during calls
    }

    LaunchedEffect(Unit) {
        UiEventBus.events.collect { event ->
            if (event is UiEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // ---------------------------------------------------------
    // 🔥 Call lifecycle → Navigation (RTM-driven)
    // ---------------------------------------------------------
    LaunchedEffect(Unit) {
        CallStore.call
            .drop(1) // skip initial null
            .collect { call ->
                when (call?.status) {
                    CallStatus.INCOMING_RINGING -> Unit

                    CallStatus.OUTGOING_RINGING,
                    CallStatus.CONNECTING,
                    CallStatus.CONNECTED -> {
                        navController.navigate(Routes.Screen.Call.ONGOING) {
                            launchSingleTop = true
                        }
                    }

                    CallStatus.ENDED, null -> {
                        Log.d("RTM", "NAVIGATE TO END")
                        when (SessionManager.userRole) {
                            UserRole.LISTENER -> {
                                navController.navigate(Routes.Graph.LISTENER) {
                                    popUpTo(Routes.Graph.CALL) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }

                            UserRole.CUSTOMER -> {
                                navController.navigate(Routes.Graph.HOME) {
                                    popUpTo(Routes.Graph.CALL) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }

                            null -> TODO()
                        }
                    }

                    else -> {}
                }
            }
    }

    // Determine start destination based on login state
    val startDestination = if (SessionManager.userAccountId != 0L && SessionManager.userRole != null) {
        when (SessionManager.userRole) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
            else -> Routes.Graph.AUTH
        }
    } else {
        Routes.Graph.AUTH
    }

    Box(Modifier.fillMaxSize()) {
        // ---------------------------------------------------------
        // 🧭 Main NavHost
        // ---------------------------------------------------------
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            authNavGraph(navController)
            homeNavGraph(navController)
            listenerNavGraph(navController)
            walletNavGraph(navController)
            callNavGraph(navController)
        }

        // ---------------------------------------------------------
        // 📣 INCOMING CALL BANNER (overlay, no layout shift)
        // ---------------------------------------------------------
        val callVm: CallViewModel = hiltViewModel()

        if (callState?.status == CallStatus.INCOMING_RINGING) {
            IncomingCallBanner(
                vm = callVm,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .zIndex(200f)
            )
        }


        // ---------------------------------------------------------
        // OVERLAY SNACKBAR
        // ---------------------------------------------------------
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .zIndex(100f)
        )
    }
}