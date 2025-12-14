package com.example.app.feature.navigation.ui

import Routes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val call by CallStore.call.collectAsState()
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
                        navController.navigate(Routes.Graph.HOME) {
                            popUpTo(Routes.Graph.CALL) { inclusive = true }
                        }
                    }

                    else -> {}
                }
            }
    }

//    LaunchedEffect(call?.status) {
//        when (call?.status) {
//            CallStatus.INCOMING_RINGING -> {
//                navController.navigate(
//                    "${Routes.Graph.CALL}/${Routes.Screen.Call.INCOMING}"
//                ) { launchSingleTop = true }
//            }
//
//            CallStatus.OUTGOING_RINGING,
//            CallStatus.CONNECTING,
//            CallStatus.CONNECTED -> {
//                navController.navigate(
////                    "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
//                    Routes.Screen.Call.ONGOING
//                ) { launchSingleTop = true }
//            }
//
//            CallStatus.ENDED, null -> {
//                navController.navigate(Routes.Graph.HOME) {
//                    popUpTo(Routes.Graph.CALL) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//
//            else -> Unit
//        }
//    }


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
