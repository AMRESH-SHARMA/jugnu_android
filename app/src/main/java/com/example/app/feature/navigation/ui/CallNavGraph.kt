package com.example.app.feature.navigation.ui

import Routes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.call.ui.IncomingCallScreen
import com.example.app.feature.call.ui.OnGoingCallScreen

fun NavGraphBuilder.callNavGraph(navController: NavHostController) {

    navigation(
        route = Routes.Graph.CALL,
        startDestination = Routes.Screen.Call.ONGOING
    ) {

        // ---------------- INCOMING CALL ----------------
        composable(route = Routes.Screen.Call.INCOMING) { backStackEntry ->
            val callVm: CallViewModel = hiltViewModel()
            CallRoot {
                IncomingCallScreen(vm = callVm)
            }
        }

        // ---------------- ONGOING CALL ----------------
        composable(route = Routes.Screen.Call.ONGOING) { backStackEntry ->
            val callVm: CallViewModel = hiltViewModel()
            CallRoot {
                OnGoingCallScreen(vm = callVm)
            }
        }
    }
}

@Composable
fun CallRoot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        content()
    }
}
