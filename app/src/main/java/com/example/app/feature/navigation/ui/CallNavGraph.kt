package com.example.app.feature.navigation.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.call.ui.IncomingCallScreen
import com.example.app.feature.call.ui.OnGoingCallScreen


fun NavGraphBuilder.callNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.INCOMING_CALL,
        route = Routes.CALL_ROOT
    ) {
        composable(Routes.INCOMING_CALL) {
            IncomingCallScreen(
                callerName = "John Doe",
                callerImage = "",
                onAccept = { navController.navigate(Routes.ONGOING_CALL) },
                onDecline = { navController.popBackStack() }
            )
        }

        composable(Routes.ONGOING_CALL) {
            OnGoingCallScreen(
                callerName = "John Doe",
                image = "",     // later pass real avatar URL or resource
                callDuration = "00:00",   // ideally come from ViewModel timer state
                onEndCall = {
                    navController.popBackStack(
                        route = "call_root",
                        inclusive = true
                    )
                },
                onToggleMute = {
                    // TODO call ViewModel action
                },
                onToggleSpeaker = {
                    // TODO route audio
                }
            )
        }

    }
}
