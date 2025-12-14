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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.feature.call.domain.CallStatus

@Composable
fun OnGoingCallScreen(
    vm: CallViewModel
) {
    // 🔥 Observe only what this screen needs
    val call by vm.callModel.collectAsState()
    val uiState by vm.uiState.collectAsState()

    val status by vm.callStatus.collectAsState()
    val isMuted = uiState.isMuted
    val isSpeakerOn = uiState.isSpeakerOn
    val duration = uiState.durationLabel

    if (status == null) return

    // Safety: screen may briefly exist while call ends
    val currentCall = call ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // ---------------- TOP INFO ----------------
        // TOP
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (status) {
                CallStatus.OUTGOING_RINGING -> Text("Calling…")
                CallStatus.CONNECTING -> Text("Connecting…")
                CallStatus.CONNECTED -> {
                    Text("In Call")
                    Spacer(Modifier.height(8.dp))
                    Text(duration)
                }

                else -> Unit
            }
        }

        // ---------------- BOTTOM CONTROLS ----------------
        // CONTROLS
        CallControls(
            status = status!!,
            isMuted = isMuted,
            isSpeakerOn = isSpeakerOn,
            onToggleMute = { vm.toggleMute() },
            onToggleSpeaker = { vm.toggleSpeaker() },
            onEndCall = vm::endCall
        )
    }
}

@Composable
private fun CallControls(
    status: CallStatus,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // MUTE (enabled only when connected)
        IconButton(
            onClick = onToggleMute,
            enabled = status == CallStatus.CONNECTED
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute"
            )
        }

        // END CALL (always enabled)
        Button(
            onClick = onEndCall,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text("End")
        }

        // SPEAKER (enabled only when connected)
        IconButton(
            onClick = onToggleSpeaker,
            enabled = status == CallStatus.CONNECTED
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speaker"
            )
        }
    }
}