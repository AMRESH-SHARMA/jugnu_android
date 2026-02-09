package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.app.feature.chat.ui.ChatScreen
import com.example.app.feature.listeners.domain.ListenerModel

fun NavGraphBuilder.chatNavGraph(navController: NavController) {
    navigation(
        route = Routes.Graph.CHAT,
        startDestination = Routes.Screen.Chat.ROOT
    ) {
        composable(
            route = Routes.Screen.Chat.ROOT,
            arguments = listOf(
                navArgument("listenerId") {
                    type = NavType.LongType
                }
            )
        ) {
            val listener = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ListenerModel>("listener")

            ChatScreen(
                navController = navController,
                listener = listener
            )
        }
    }
}
