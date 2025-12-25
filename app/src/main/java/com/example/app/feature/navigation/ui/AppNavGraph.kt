package com.example.app.feature.navigation.ui

import Routes
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.core.call.CallStore
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.core.session.SessionManager
import com.example.app.core.ui.UiEvent
import com.example.app.core.ui.UiEventBus
import com.example.app.feature.call.domain.CallStatus
import kotlinx.coroutines.flow.drop

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    CallStatus.INCOMING_RINGING -> {
                        navController.navigate(Routes.Screen.Call.INCOMING)
                    }

                    CallStatus.OUTGOING_RINGING,
                    CallStatus.CONNECTING,
                    CallStatus.CONNECTED -> {
                        navController.navigate(Routes.Screen.Call.ONGOING)
                    }

                    CallStatus.ENDED, null -> {
                        Log.d("RTM", "NAVIAGTE TO END")
                        when (SessionManager.userRole) {
                            UserRole.LISTENER -> {
                                navController.navigate(Routes.Graph.LISTENER) {
                                    popUpTo(Routes.Graph.CALL) { inclusive = true }
                                }
                            }

                            UserRole.CUSTOMER -> {
                                navController.navigate(Routes.Graph.HOME) {
                                    popUpTo(Routes.Graph.CALL) { inclusive = true }
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
        // ---------------------------------------------------------
        // 🧭 Main NavHost
        // ---------------------------------------------------------
        NavHost(
            navController = navController,
            startDestination = Routes.Graph.SELECT_USER_ROLE
        ) {
            selectUserRoleNavGraph(navController)
            homeNavGraph(navController)
            listenerNavGraph(navController)
            walletNavGraph(navController)
            callNavGraph(navController)
        }
        // OVERLAY SNACKBAR
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .zIndex(100f)
        )
    }
}