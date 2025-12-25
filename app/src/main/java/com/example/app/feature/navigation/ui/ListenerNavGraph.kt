package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.listenerDashboard.ui.ListenerDashboardScreen

// Listener navigation graph (listener-specific screens)
fun NavGraphBuilder.listenerNavGraph(navController: NavHostController) {

    navigation(
        route = Routes.Graph.LISTENER,
        startDestination = Routes.Screen.Listener.ListenerDashboard
    ) {
        composable(Routes.Screen.Listener.ListenerDashboard) {
            ListenerDashboardScreen(navController)
        }

        // Add more listener screens later, e.g.:
        // composable(Routes.Screen.Listener.Earnings) { ... }
        // composable(Routes.Screen.Listener.Calls) { ... }
    }
}
