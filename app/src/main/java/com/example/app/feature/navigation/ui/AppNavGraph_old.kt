package com.example.app.feature.navigation.ui

//@Composable
//fun AppNavGraph(
////    route: String?,
////    callerAccountId: Long,
////    calleeAccountId: Long,
////    callId: String?
//) {
//    val navController = rememberNavController()
//    val callVm: CallViewModel = hiltViewModel()
////    val callState by callVm.callState.collectAsState()
//    val callLifecycle by callVm.callLifecycle.collectAsState()
//
//    LaunchedEffect(Unit) {
//        callVm.callLifecycle.collect { state ->
//            Log.d("RTM", "NAV reacting to lifecycle=$state")
//
//            when (state) {
//                CallLifecycleState.Ongoing -> {
//                    Log.d("RTM", "NAV -> ongoing_call")
//                    navController.navigate(
//                        "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
//                    ) {
//                        launchSingleTop = true
//                    }
//                }
//
//                CallLifecycleState.Incoming -> { /* ... */
//                }
//
//                CallLifecycleState.Ended -> { /* ... */
//                }
//
//                else -> Unit
//            }
//        }
//    }
//
//
////    LaunchedEffect(callLifecycle) {
////        Log.d("RTM", "NAV reacting to lifecycle=$callLifecycle")
////
////        when (callLifecycle) {
////
////            CallLifecycleState.Incoming -> {
////                Log.d("RTM", "NAV -> enter CALL graph")
////                navController.navigate(Routes.Graph.CALL) {
////                    launchSingleTop = true
////                }
////            }
////
////            CallLifecycleState.Ongoing -> {
////                Log.d("RTM", "NAV -> ongoing_call")
////
////                navController.navigate(
////                    "${Routes.Graph.CALL}/${Routes.Screen.Call.ONGOING}"
////                ) {
////                    launchSingleTop = true
////                }
//////                // 🔥 ENSURE graph is present
//////                navController.navigate(Routes.Graph.CALL) {
//////                    launchSingleTop = true
//////                }
//////
//////                // 🔥 THEN navigate inside it
//////                navController.navigate(Routes.Screen.Call.ONGOING) {
//////                    popUpTo(Routes.Graph.CALL)
//////                }
////            }
////
////            CallLifecycleState.Ended -> {
////                Log.d("RTM", "NAV -> HOME")
////                navController.navigate(Routes.Graph.HOME) {
////                    popUpTo(Routes.Graph.CALL) { inclusive = true }
////                    launchSingleTop = true
////                }
////            }
////
////            CallLifecycleState.Idle -> Unit
////        }
////    }
//
//    // ---------------------------------------------------------
//    // 1️⃣ FCM / Intent-based navigation (fallback entry point)
//    // ---------------------------------------------------------
////    LaunchedEffect(route) {
////        when (route) {
////
////            AppConstants.EVENT_INCOMING_CALL -> {
////                val listener = ListenerModel(
////                    accountId = callerAccountId,
////                    name = "Unknown",
////                    avatar = "",
////                    tagLine = "",
////                    about = "",
////                    age = 0,
////                    gender = "",
////                    experience = 0,
////                    rating = 0.0
////                )
////                navController.openIncomingCall(listener)
////            }
////
////            AppConstants.EVENT_CALL_ENDED,
////            AppConstants.EVENT_CALL_REJECTED -> {
////                navController.navigate(Routes.HOME) {
////                    popUpTo(Routes.HOME) { inclusive = true }
////                }
////            }
////        }
////    }
//
//    // ---------------------------------------------------------
//    // 2️⃣ RTM / CallEventBus navigation (PRIMARY PATH)
//    // ---------------------------------------------------------
////    LaunchedEffect(Unit) {
////        Log.d("RTM", "AppNavGraph CREATED navController=$navController")
////        CallEventBus.events.collect { event ->
////            when (event) {
////
////                is CallEvent.Incoming -> {
//////                    val listener = ListenerModel(
//////                        accountId = event.callerAccountId,
//////                        name = "Unknown",
//////                        avatar = "",
//////                        tagLine = "",
//////                        about = "",
//////                        age = 0,
//////                        gender = "",
//////                        experience = 0,
//////                        rating = 0.0
//////                    )
//////                    navController.openIncomingCall(listener)
////                }
////
////                is CallEvent.Accepted -> {
////                    // Optional: if you want auto-navigation on accept
////                    // navController.openOngoingCall(...)
////                }
////
////                is CallEvent.Cancelled,
////                is CallEvent.Ended,
////                is CallEvent.Rejected -> {
////                    navController.navigate(Routes.Graph.HOME) {
////                        popUpTo(Routes.Graph.CALL) { inclusive = true }
////                    }
////                }
////
////                else -> Unit
////            }
////        }
////    }
//
//    // ---------------------------------------------------------
//    // 3️⃣ Normal app navigation
//    // ---------------------------------------------------------
//    NavHost(
//        navController = navController,
//        startDestination = Routes.Graph.SELECT_USER_ROLE
//    ) {
//        selectUserRoleNavGraph(navController)
//        homeNavGraph(navController)
//        walletNavGraph(navController)
//        callNavGraph(navController)
//    }
//}

