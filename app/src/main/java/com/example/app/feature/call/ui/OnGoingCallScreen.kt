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
