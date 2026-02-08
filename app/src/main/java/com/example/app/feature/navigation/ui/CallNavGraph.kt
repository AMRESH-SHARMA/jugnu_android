package com.example.app.feature.navigation.ui

import Routes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.call.ui.OnGoingCallScreen

fun NavGraphBuilder.callNavGraph(navController: NavHostController) {

    navigation(
        route = Routes.Graph.CALL,
        startDestination = Routes.Screen.Call.ONGOING
    ) {

        // Ongoing call screen (main call UI)
        composable(route = Routes.Screen.Call.ONGOING) { backStackEntry ->
            val callVm: CallViewModel = hiltViewModel()
            CallRoot {
                OnGoingCallScreen(
                    vm = callVm
                    // Navigation handled by CallStore observer in AppNavGraph
                )
            }
        }

        // Note: Incoming call is shown as overlay banner in AppNavGraph
        // Not a separate screen to avoid navigation complexity
    }
}

@Composable
fun CallRoot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        content()
    }
}
