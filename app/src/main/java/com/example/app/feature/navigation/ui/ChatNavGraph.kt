package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.chat.ui.ChatScreen
import com.example.app.feature.listeners.domain.ListenerModel

object ChatRoutes {
    const val ROOT = "chat_root"
    const val CHAT = "chat"

//    fun createRoute(chatId: String) = "chat/$chatId"
}

fun NavGraphBuilder.chatNavGraph(navController: NavController, onBack: () -> Boolean) {
    navigation(
        startDestination = ChatRoutes.CHAT,
        route = ChatRoutes.ROOT
    ) {
        composable(route = ChatRoutes.CHAT) {
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


//fun NavGraphBuilder.chatNavGraph(navController: NavController, onBack: () -> Boolean) {
//    navigation(
//        startDestination = ChatRoutes.CHAT,
//        route = ChatRoutes.ROOT
//    ) {
//        composable(
//            route = ChatRoutes.CHAT,
//            arguments = listOf(
////                navArgument("chatId") { type = NavType.StringType }
//                navArgument("listener") {
//                    type = NavType.ParcelableType(Listener::class.java)
//                }
//            )
//        ) { backStackEntry ->
////            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
//            val listener = backStackEntry.arguments?.getParcelable<Listener>("listener")!!
//            ChatScreen(
//                navController = navController,
//                listener = listener,
//                onBack = { navController.popBackStack() }
//            )
//        }
//    }
//}


//fun NavController.openChat(chatId: String) {
//    navigate(ChatRoutes.createRoute(chatId))
//}
fun NavController.openChat(listenerModel: ListenerModel) {
    this.currentBackStackEntry?.savedStateHandle?.set("listener", listenerModel)
    navigate(ChatRoutes.CHAT)
}