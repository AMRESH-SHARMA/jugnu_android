package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.home.ui.HomeScreen
import com.example.app.feature.home.ui.HomeTab


fun NavGraphBuilder.homeNavGraph(navController: NavController) {

    navigation(
        route = Routes.Graph.HOME,
        startDestination = Routes.Screen.Home.ROOT
    ) {
        composable(Routes.Screen.Home.ROOT) {
            HomeScreen(
                navController,
                onContactClick = { tab, listener ->
                    if (tab == HomeTab.CHATS) {
                        navController.openChat(listener)
                    }
                }

            )
        }
    }

    chatNavGraph(
        navController = navController,
        onBack = { navController.popBackStack() }
    )
    walletNavGraph(
        navController = navController,
    )
}
