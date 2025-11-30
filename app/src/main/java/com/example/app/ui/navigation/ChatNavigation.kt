package com.example.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.app.ui.chat.ChatScreen

object ChatRoutes {
    const val CHAT = "chat/{chatId}"

    fun createRoute(chatId: String) = "chat/$chatId"
}

fun NavGraphBuilder.chatNavGraph(
    onBack: () -> Unit
) {
    composable(
        route = ChatRoutes.CHAT,
        arguments = listOf(
            navArgument("chatId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""

        ChatScreen(
            chatId = chatId,
            onBack = onBack
        )
    }
}

fun NavController.openChat(chatId: String) {
    navigate(ChatRoutes.createRoute(chatId))
}
