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
        startDestination = Routes.Screen.Listener.DASHBOARD
    ) {
        // Listener Dashboard (Main screen)
        composable(Routes.Screen.Listener.DASHBOARD) {
            ListenerDashboardScreen(
                navController = navController,
                onWalletClick = {
                    navController.navigate(Routes.Graph.WALLET) {
                        launchSingleTop = true
                    }
                }
                // Future: onCustomerListClick = { ... }
            )
        }

        // Future screens:
        // composable(Routes.Screen.Listener.CUSTOMER_LIST) {
        //     CustomerListScreen(
        //         navController = navController,
        //         onCustomerClick = { customer ->
        //             // Initiate call to customer
        //         }
        //     )
        // }
    }

    // Wallet graph (shared with customer but different features)
    walletNavGraph(navController)
}
