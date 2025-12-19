package com.example.app.feature.navigation.ui

import Routes
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.core.call.CallStore
import com.example.app.feature.call.domain.CallStatus
import kotlinx.coroutines.flow.drop

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

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
                        navController.navigate(Routes.Graph.HOME) {
                            popUpTo(Routes.Graph.CALL) { inclusive = true }
                        }
                    }

                    else -> {}
                }
            }
    }

    // ---------------------------------------------------------
    // 🧭 Main NavHost
    // ---------------------------------------------------------
    NavHost(
        navController = navController,
        startDestination = Routes.Graph.SELECT_USER_ROLE
    ) {
        selectUserRoleNavGraph(navController)
        homeNavGraph(navController)
        walletNavGraph(navController)
        callNavGraph(navController)
    }
}
