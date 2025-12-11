package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.home.ui.HomeScreen
import com.example.app.feature.home.ui.HomeTab

object HomeRoutes {
    const val ROOT = "home_root"
}

fun NavGraphBuilder.homeNavGraph(navController: NavController) {

    navigation(
        startDestination = HomeRoutes.ROOT,
        route = Routes.HOME
    ) {
        composable(HomeRoutes.ROOT) {
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
