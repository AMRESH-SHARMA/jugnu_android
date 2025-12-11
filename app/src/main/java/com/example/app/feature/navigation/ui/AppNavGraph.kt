package com.example.app.feature.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.utils.AppConstants


@Composable
fun AppNavGraph(
    route: String?,
    callerId: Long,
    calleeId: Long,
    callId: String?
) {
    val navController = rememberNavController()
    LaunchedEffect(route) {
        when (route) {
            AppConstants.EVENT_INCOMING_CALL -> {
                val listener = ListenerModel(
                    accountId = callerId,
                    name = "Unknown",
                    avatar = "",
                    tagLine = "",
                    about = "",
                    age = 0,
                    gender = "",
                    experience = 0,
                    rating = 0.0
                )
                navController.openIncomingCall(listener)
            }

            AppConstants.EVENT_CALL_ENDED,
            AppConstants.EVENT_CALL_REJECTED -> {
                // remove call stack and go home
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            }

        }
    }
    NavHost(
        navController = navController,
        startDestination = Routes.SELECT_USER_ROLE
    ) {
//        authNavGraph(navController)
        selectUserRoleNavGraph(navController)
        homeNavGraph(navController)
        walletNavGraph(navController)
        callNavGraph(navController)
    }
}