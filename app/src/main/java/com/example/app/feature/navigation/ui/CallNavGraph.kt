package com.example.app.feature.navigation.ui

import Routes
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.call.ui.OnGoingCallScreen

fun NavGraphBuilder.callNavGraph(navController: NavHostController) {

//    Log.d("RTM", "callNavGraph REGISTERED")

    navigation(
        route = Routes.Graph.CALL,
        startDestination = Routes.Screen.Call.ONGOING
    ) {

        // ---------------- INCOMING CALL ----------------
        composable(
            route = Routes.Screen.Call.INCOMING
        ) {
//            Log.d("RTM", "Inside IncomingCallScreen composable")

            val parentEntry = remember {
                navController.getBackStackEntry(Routes.Graph.CALL)
            }

            val callVm: CallViewModel = hiltViewModel(parentEntry)

//            IncomingCallScreen(vm = callVm)
        }

        // ---------------- ONGOING CALL ----------------
        composable(
            route = Routes.Screen.Call.ONGOING
        ) {
//            Log.d("RTM", "Inside OnGoingCallScreen composable")

            val parentEntry = remember {
                navController.getBackStackEntry(Routes.Graph.CALL)
            }

            val callVm: CallViewModel = hiltViewModel(parentEntry)

            OnGoingCallScreen(vm = callVm)
        }
    }
}


//fun NavGraphBuilder.callNavGraph(navController: NavHostController) {
//    Log.d("RTM", "callNavGraph REGISTERED")
//    navigation(
//        route = Routes.Graph.CALL,
//        startDestination = "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
////        startDestination = Routes.Screen.Call.INCOMING
//    ) {
//
//        composable(
////            Routes.Screen.Call.INCOMING
//            route = "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
//        ) {
//            val callVm: CallViewModel =
//                hiltViewModel(navController.getBackStackEntry(Routes.Graph.CALL))
//
////            IncomingCallScreen(vm = callVm)
//        }
//
//        composable(
////            Routes.Screen.Call.ONGOING
//            route = "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
//        ) {
//            Log.d("RTM", "Inside OnGoingCallScreen composable")
//
//            val parentEntry = remember {
//                navController.getBackStackEntry(Routes.Graph.CALL)
//            }
//
//            val callVm: CallViewModel = hiltViewModel(parentEntry)
//
//            OnGoingCallScreen(vm = callVm)
//        }
//    }
//}
