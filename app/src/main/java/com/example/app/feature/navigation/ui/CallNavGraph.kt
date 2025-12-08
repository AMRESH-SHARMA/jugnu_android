package com.example.app.feature.navigation.ui

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.app.feature.call.ui.IncomingCallScreen
import com.example.app.feature.call.ui.OnGoingCallScreen
import com.example.app.feature.listeners.domain.ListenerModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/* ------------ NAVIGATION HELPERS ---------------- */

fun NavController.openIncomingCall(listener: ListenerModel) {
    val json = Uri.encode(Json.encodeToString(listener))
    navigate("incoming_call?listener=$json")
}

fun NavController.openOngoingCall(listener: ListenerModel) {
    val json = Uri.encode(Json.encodeToString(listener))
    navigate("ongoing_call?listener=$json")
}

/* ------------ NAV GRAPH ---------------- */

fun NavGraphBuilder.callNavGraph(navController: NavHostController) {

    navigation(
        startDestination = "incoming_call",
        route = Routes.CALL_ROOT
    ) {

        /** Incoming */
        composable(
            route = "incoming_call?listener={listener}",
            arguments = listOf(
                navArgument("listener") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->

            val json = backStackEntry.arguments?.getString("listener")

            val listener =
                json?.let { Json.decodeFromString<ListenerModel>(Uri.decode(it)) }

            IncomingCallScreen(
                listener = listener!!,
                navController = navController
            )
        }

        /** Ongoing */
        composable(
            route = Routes.ONGOING_CALL,
            arguments = listOf(
                navArgument("listener") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->

            val json = backStackEntry.arguments?.getString("listener")

            val listener =
                json?.let { Json.decodeFromString<ListenerModel>(Uri.decode(it)) }

            OnGoingCallScreen(
                listener = listener!!,
                navController = navController
            )
        }
    }
}
