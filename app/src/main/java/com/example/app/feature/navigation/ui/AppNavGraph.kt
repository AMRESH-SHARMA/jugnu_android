package com.example.app.feature.navigation.ui

import Routes
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.core.call.CallStore
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.core.session.SessionManager
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.call.ui.IncomingCallBanner
import kotlinx.coroutines.flow.drop

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Observe call state for UI overlays
    val callState by CallStore.call.collectAsState(initial = null)

    // Disable back button during incoming or ongoing calls
    androidx.activity.compose.BackHandler(
        enabled = callState?.status == CallStatus.INCOMING_RINGING ||
                  callState?.status == CallStatus.OUTGOING_CONNECTING ||
                  callState?.status == CallStatus.OUTGOING_RINGING ||
                  callState?.status == CallStatus.CONNECTING ||
                  callState?.status == CallStatus.CONNECTED
    ) {
        // Do nothing - prevent back navigation during calls
    }

    // ---------------------------------------------------------
    // 🔥 Call lifecycle → Navigation (RTM-driven)
    // ---------------------------------------------------------
    LaunchedEffect(Unit) {
        var isOnCallScreen = false
        CallStore.call
            .drop(1) // skip initial null
            .collect { call ->
                when (call?.status) {
                    CallStatus.INCOMING_RINGING -> Unit

                    CallStatus.OUTGOING_CONNECTING,
                    CallStatus.OUTGOING_RINGING,
                    CallStatus.CONNECTING,
                    CallStatus.CONNECTED -> {
                        // Only navigate if not already on call screen
                        if (!isOnCallScreen) {
                            navController.navigate(Routes.Screen.Call.ONGOING) {
                                launchSingleTop = true
                            }
                            isOnCallScreen = true
                        }
                    }

                    CallStatus.ENDED, null -> {
                        isOnCallScreen = false
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
        // Check notification permission for Android 13+
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Auto-granted on Android 12 and below
        }
        
        // If notification permission is missing, redirect to permission screen
        if (!hasNotificationPermission) {
            Routes.Graph.AUTH  // Will show PERMISSION via authNavGraph logic
        }
        // Check if profile is complete for customers
        else if (SessionManager.userRole == UserRole.CUSTOMER && !SessionManager.isProfileComplete) {
            Routes.Graph.AUTH  // Will show PROFILE_SETUP via authNavGraph logic
        } else {
            when (SessionManager.userRole) {
                UserRole.LISTENER -> Routes.Graph.LISTENER
                UserRole.CUSTOMER -> Routes.Graph.HOME
                else -> Routes.Graph.AUTH
            }
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
    }
}