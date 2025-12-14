package com.example.app.feature.call.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnGoingCallScreen(
    vm: CallViewModel
) {
    val call by vm.callModel.collectAsState()
    val ui by vm.uiState.collectAsState()

    // Safety: screen may briefly exist while call ends
    if (call == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // ---------------- CALL INFO ----------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "In Call",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Call ID: ${call!!.callId}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = ui.durationLabel,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // ---------------- ACTION BUTTONS ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // MUTE
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = "Mute"
                )
            }

            // END CALL
            Button(
                onClick = { vm.endCall() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("End")
            }

            // SPEAKER
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaker"
                )
            }
        }
    }
}

/*
package com.example.app.feature.call.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.domain.CallUiState
import com.example.app.feature.call.viewmodel.CallViewModel

@Composable
fun OngoingCallScreen(
    vm: CallViewModel
) {
    // 🔥 Collect ONLY call state here
    val call by vm.callModel.collectAsState()

    // Screen can briefly exist while call is ending
    if (call == null || call!!.status != CallStatus.CONNECTED) {
        return
    }

    // Screen-entry side effects (runs once per entry)
    LaunchedEffect(call!!.callId) {
        vm.startTimer()
    }

    DisposableEffect(call!!.callId) {
        onDispose {
            vm.stopTimer()
        }
    }

    OngoingCallContent(
        call = call!!,
        vm = vm
    )
}

// ----------------------------------------------------------------------
// CONTENT
// ----------------------------------------------------------------------

@Composable
private fun OngoingCallContent(
    call: CallModel,
    vm: CallViewModel
) {
    // 🔥 Collect UI state ONLY where needed
    val ui by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CallHeader(call = call)

        CallDuration(durationLabel = ui.durationLabel)

        CallControls(
            ui = ui,
            onToggleMute = vm::toggleMute,
            onToggleSpeaker = vm::toggleSpeaker,
            onEndCall = vm::endCall
        )
    }
}

// ----------------------------------------------------------------------
// HEADER (rarely recomposes)
// ----------------------------------------------------------------------

@Composable
private fun CallHeader(call: CallModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "In Call",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Call ID: ${call.callId.take(8)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ----------------------------------------------------------------------
// DURATION (updates every second only)
// ----------------------------------------------------------------------

@Composable
private fun CallDuration(
    durationLabel: String
) {
    Text(
        text = durationLabel,
        style = MaterialTheme.typography.titleLarge
    )
}

// ----------------------------------------------------------------------
// CONTROLS (local UI recomposition only)
// ----------------------------------------------------------------------

@Composable
private fun CallControls(
    ui: CallUiState,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onToggleMute) {
            Icon(
                imageVector = if (ui.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute"
            )
        }

        FloatingActionButton(
            onClick = onEndCall,
            containerColor = Color.Red
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White
            )
        }

        IconButton(onClick = onToggleSpeaker) {
            Icon(
                imageVector = if (ui.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Speaker"
            )
        }
    }
}

* */