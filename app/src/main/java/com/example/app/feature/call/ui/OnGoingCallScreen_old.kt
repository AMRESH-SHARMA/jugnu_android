package com.example.app.feature.call.ui


//@Composable
//fun OnGoingCallScreen(
//    vm: CallViewModel,
////    listener: ListenerModel,
////    navController: NavController
//) {
//
//    val ui by vm.uiState.collectAsState()
//    val callModel by vm.callModel.collectAsState()
//    val lifecycle by vm.callLifecycle.collectAsState()
//
//    // 🔐 Safety: no call → no UI
//    if (callModel == null) {
//        // 🔥 Show loading instead of returning
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Connecting…", style = MaterialTheme.typography.bodyMedium)
//        }
//        return
//    }
//
//    val call = callModel!!
//    // -----------------------------
//    // 1️⃣ START TIMER WHEN ACCEPTED
//    // -----------------------------
//    LaunchedEffect(lifecycle) {
//
//        if (lifecycle == CallLifecycleState.Ongoing) {
//            vm.startTimer()
//        } else {
//            vm.stopTimer()
//        }
//    }
//
//    // -----------------------------
//    // 2️⃣ UI STATE (MUTE, SPEAKER)
//    // -----------------------------
//    var isMuted by remember { mutableStateOf(false) }
//    var isSpeakerOn by remember { mutableStateOf(false) }
//
//    // -----------------------------
//    // 3️⃣ UI LAYOUT
//    // -----------------------------
//    Box(Modifier.fillMaxSize()) {
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 60.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            AsyncImage(
////                model = call.calleeAvatar,
//                model = "aa",
//                contentDescription = null,
//                modifier = Modifier
//                    .size(150.dp)
//                    .clip(CircleShape)
//            )
//
//
//            Text(
//                text = "r",
//                style = MaterialTheme.typography.headlineMedium
//            )
//
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = when (CallStatus.RINGING) {
//                    CallStatus.RINGING -> "Calling…"
//                    CallStatus.ACCEPTED -> ui.durationLabel
//                    else -> ""
//                },
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//
//        //--------------------------------
//        // 4️⃣ CALL CONTROL BUTTONS
//        //--------------------------------
//        Row(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 32.dp)
//        ) {
//
//            // MUTE
//            IconButton(
//                onClick = {
//                    isMuted = !isMuted
//                    vm.toggleMute()
//                }
//            ) {
//                Icon(
//                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
//                    contentDescription = null
//                )
//            }
//
//            // SPEAKER
//            IconButton(
//                onClick = {
//                    isSpeakerOn = !isSpeakerOn
//                    vm.toggleSpeaker()
//                }
//            ) {
//                Icon(
//                    imageVector =
//                        if (isSpeakerOn)
//                            Icons.AutoMirrored.Filled.VolumeUp
//                        else
//                            Icons.AutoMirrored.Filled.VolumeOff,
//                    contentDescription = null
//                )
//            }
//
//            // END CALL
//            FloatingActionButton(
//                onClick = {
//                    Log.d("RTM", "CALL End call button CLICKED")
//
//                    // 1️⃣ RTM End call FIRST (fire-and-forget)
//                    vm.endCall()
//
////                    Log.d("RTM", "Before CALL End Navigation button CLICKED")
//                    // 2️⃣ Navigate AFTER (do not wait for RTM)
////                    navController.navigate(Routes.Graph.HOME) {
////                        popUpTo(Routes.Screen.Call.ROOT) { inclusive = true }
////                        launchSingleTop = true
////                    }
//                },
//                containerColor = Color.Red,
//                shape = CircleShape,
//                modifier = Modifier.size(60.dp)
//            ) {
//                Icon(Icons.Default.CallEnd, contentDescription = null)
//            }
//        }
//    }
//}