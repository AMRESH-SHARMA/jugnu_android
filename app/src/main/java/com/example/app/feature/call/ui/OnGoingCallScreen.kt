package com.example.app.feature.call.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.core.user.SessionViewModel
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.navigation.ui.Routes
import kotlinx.coroutines.launch

@Composable
fun OnGoingCallScreen(
    listener: ListenerModel,
    navController: NavController,
) {
    val vm: CallViewModel = hiltViewModel()
    val sessionVm: SessionViewModel = hiltViewModel()
    val session = sessionVm.session

    val ui by vm.uiState.collectAsState()
    val callState by vm.callState.collectAsState()
    val scope = rememberCoroutineScope()

    // -----------------------------
    // 1) LISTEN FOR BACKEND EVENTS
    // -----------------------------
    LaunchedEffect(Unit) {
        CallEventBus.events.collect { event ->
            when (event) {

                is CallEvent.CallRejected,
                is CallEvent.CallEnded -> {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CALL_ROOT) { inclusive = true }
                    }
                }

                else -> {}
            }
        }
    }

    // -----------------------------
    // 2) START CALL ON MOUNT
    // -----------------------------
    val sessionData by session.sessionFlow.collectAsState()
    val loggedInUserId = sessionData.first

    LaunchedEffect(loggedInUserId, listener.accountId) {
        if (loggedInUserId > 0L) {
            vm.startCall(
                callerId = loggedInUserId,
                calleeId = listener.accountId
            )
        }
    }

    // -----------------------------
    // 3) START TIMER WHEN ACCEPTED
    // -----------------------------
    val status = callState?.status
    LaunchedEffect(status) {
        if (status == "ACCEPTED") {
            vm.startTimer()
        }
    }

    // -----------------------------
    // 4) UI STATE (MUTE, SPEAKER)
    // -----------------------------
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    // -----------------------------
    // 5) UI LAYOUT
    // -----------------------------
    Box(Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = listener.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            Text(listener.name, style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(4.dp))

            Text(
                text = when (status) {
                    "ACCEPTED" -> ui.durationLabel
                    else -> "Ringing…"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //--------------------------------
        // 6) CALL CONTROL BUTTONS
        //--------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {

            // MUTE
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    vm.toggleMute()
                }
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                )
            }

            // SPEAKER
            IconButton(
                onClick = {
                    isSpeakerOn = !isSpeakerOn
                    vm.toggleSpeaker()
                }
            ) {
                Icon(
                    imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = null,
                )
            }

            // END CALL
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        vm.endCall()
                    }
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CALL_ROOT) { inclusive = true }
                    }
                },
                containerColor = Color.Red,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }
        }
    }
}

//@Composable
//fun OnGoingCallScreen(
//    listener: ListenerModel,
//    navController: NavController,
//) {
//    val vm: CallViewModel = hiltViewModel()
//    val sessionVm: SessionViewModel = hiltViewModel()
//    val session = sessionVm.session
//
//    val ui by vm.uiState.collectAsState()
//    val callState by vm.callState.collectAsState()
//
//    // START TIMER only when accepted
//    val sessionData by session.sessionFlow.collectAsState()
//    val loggedInUserId = sessionData.first
//    LaunchedEffect(loggedInUserId, listener.accountId) {
//        if (loggedInUserId > 0L) {
//            vm.startCall(
//                callerId = loggedInUserId,
//                calleeId = listener.accountId
//            )
//        }
//    }
//
//    // START TIMER only when accepted
//    val status = callState?.status
//    LaunchedEffect(status) {
//        if (status == "ACCEPTED") {
//            vm.startTimer()
//        }
//    }
//
//
//    var isMuted by remember { mutableStateOf(false) }
//    var isSpeakerOn by remember { mutableStateOf(false) }
//
//    Box(Modifier.fillMaxSize()) {
//
//        Column(
//            Modifier
//                .fillMaxSize()
//                .padding(top = 60.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            AsyncImage(
//                model = listener.avatar,
//                contentDescription = null,
//                modifier = Modifier
//                    .size(150.dp)
//                    .clip(CircleShape)
//            )
//
//            Text(listener.name, style = MaterialTheme.typography.headlineMedium)
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = when (status) {
//                    "ACCEPTED" -> ui.durationLabel
//                    else -> "Ringing…"
//                },
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//
//        Row(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 32.dp)
//        ) {
//
//            IconButton(
//                onClick = {
//                    isMuted = !isMuted
//                    vm.toggleMute()
//                }
//            ) {
//                Icon(
//                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
//                    contentDescription = null,
//                )
//            }
//
//            IconButton(
//                onClick = {
//                    isSpeakerOn = !isSpeakerOn
//                    vm.toggleSpeaker()
//                }
//            ) {
//                Icon(
//                    imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
//                    contentDescription = null,
//                )
//            }
//
//            FloatingActionButton(
//                onClick = {
//                    vm.endCall()
////                    navController.popBackStack(Routes.CALL_ROOT, inclusive = true)
//                    navController.navigate(Routes.HOME) {
//                        popUpTo(Routes.HOME) { inclusive = false }
//                        launchSingleTop = true
//                    }
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