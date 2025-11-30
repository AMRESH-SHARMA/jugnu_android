package com.example.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.ui.home.HomeScreen
import com.example.app.ui.home.HomeTab

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
                onContactClick = { tab, chatId ->
                    if (tab == HomeTab.CHATS) {
                        navController.openChat(chatId)
                    }
                }
            )
        }
    }

    chatNavGraph(
        onBack = { navController.popBackStack() }
    )
}
