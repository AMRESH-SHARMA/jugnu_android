package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.chat.ui.ChatScreen
import com.example.app.feature.listeners.domain.ListenerModel

fun NavGraphBuilder.chatNavGraph(navController: NavController, onBack: () -> Boolean) {
    navigation(
        route = Routes.Graph.CHAT,
        startDestination = Routes.Screen.Chat.ROOT
    ) {
        composable(Routes.Screen.Chat.ROOT) {
            val listenerModel =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<ListenerModel>("listener")

            ChatScreen(
                navController = navController,
                listenerModel = listenerModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}


fun NavController.openChat(listenerModel: ListenerModel) {
    this.currentBackStackEntry?.savedStateHandle?.set("listener", listenerModel)
    navigate(Routes.Graph.CHAT)
}