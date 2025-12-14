package com.example.app.feature.navigation.ui

//fun NavGraphBuilder.callNavGraph(
//    navController: NavHostController
//) {
//    navigation(
//        route = Routes.Graph.CALL,
//        startDestination = Routes.Screen.Call.INCOMING
//    ) {
//        Log.d("RTM", "Before OnGoingCallScreen composable")
//        composable(Routes.Screen.Call.INCOMING) {
////            val callVm: CallViewModel = hiltViewModel(navController.getBackStackEntry(Routes.Graph.CALL))
////            IncomingCallScreen(vm = callVm)
//        }
//
//        composable(Routes.Screen.Call.ONGOING) {
//            Log.d("RTM", "Inside OnGoingCallScreen composable")
//            val callVm: CallViewModel =
//                hiltViewModel(navController.getBackStackEntry(Routes.Graph.CALL))
//            OnGoingCallScreen(vm = callVm)
//        }
//    }
//}

///* ------------ NAVIGATION HELPERS ---------------- */
//
//fun NavController.openIncomingCall(listener: ListenerModel) {
////    val json = Uri.encode(Json.encodeToString(listener))
////    navigate("${Routes.CALL_ROOT}/${Routes.INCOMING_CALL}?listener=$json")
//}
//
//fun NavController.openOngoingCall(listener: ListenerModel) {
//    val json = Uri.encode(Json.encodeToString(listener))
////    val route = "${Routes.Screen.Call.ROOT}/${Routes.Screen.Call.ONGOING}?listener=$json"
//    val route = "${Routes.Screen.Call.ROOT}/${Routes.Screen.Call.ONGOING}"
//    Log.d("RTM", "openOngoingCall Navigating to: $route")
//    navigate(route)
//}
//
///* ------------ NAV GRAPH ---------------- */
//
//fun NavGraphBuilder.callNavGraph(navController: NavHostController) {
//
//    navigation(
//        route = Routes.Graph.CALL,
//        startDestination = Routes.Screen.Call.INCOMING
//    ) {
//        Log.d("RTM", "Inside navigation")
//        // ---------------- INCOMING ----------------
////        composable(
////            route = "${Routes.INCOMING_CALL}?listener={listener}",
////            arguments = listOf(
////                navArgument("listener") {
////                    type = NavType.StringType
////                    nullable = true
////                }
////            )
////        )
////        { backStackEntry ->
////
////            val callVm: CallViewModel =
////                hiltViewModel(navController.getBackStackEntry(Routes.CALL_ROOT))
////
////            val json = backStackEntry.arguments?.getString("listener")
////            val listener =
////                json?.let { Json.decodeFromString<ListenerModel>(Uri.decode(it)) }
////
////            IncomingCallScreen(
////                vm = callVm,
////                listener = listener!!,
////                navController = navController
////            )
////        }
//
//
//        // ---------------- ONGOING ----------------
//        composable(
////            route = Routes.Screen.Call.ONGOING,
//            route = "call/ongoing_call",
//            arguments = listOf(
//                navArgument("listener") {
//                    type = NavType.StringType
//                    nullable = true
//                }
//            )
//
//        ) { backStackEntry ->
//            Log.d("RTM", "Inside Composable")
//            val callVm: CallViewModel =
//                hiltViewModel(navController.getBackStackEntry(Routes.Screen.Call.ROOT))
//
//            val json = backStackEntry.arguments?.getString("listener")
//            val listener = json?.let { Json.decodeFromString<ListenerModel>(Uri.decode(it)) }
//
//            Log.d("RTM", "Before OngoingCall Screen")
//            OnGoingCallScreen(
//                vm = callVm,
//                listener = listener!!,
//                navController = navController
//            )
//        }
//    }
//}
