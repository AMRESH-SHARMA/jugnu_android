package com.example.app.feature.call.ui
/*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.feature.call.domain.CallStatus

@Composable
fun IncomingCallScreen(
    vm: CallViewModel
) {
    // 🔥 Observe call state only
    val call by vm.callModel.collectAsState()
    val header by vm.headerUiState.collectAsState()

    // Safety guard
    if (call == null || call!!.status != CallStatus.INCOMING_RINGING) {
        return
    }

    IncomingCallContent(
        header = header,
        callType = call!!.callType,
        onAccept = vm::acceptCall,
        onReject = vm::rejectCall
    )
}

// ----------------------------------------------------------------------
// CONTENT
// ----------------------------------------------------------------------

@Composable
private fun IncomingCallContent(
    header: CallHeaderUiState,
    callType: CallType,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ---------------- CALL INFO ----------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Avatar
            if (header.isLoading) {
                CircularProgressIndicator()
            } else {
                AsyncImage(
                    model = header.avatarUrl,
                    contentDescription = "Caller Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = if (header.isLoading) "Calling…" else header.name,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = when (callType) {
                    CallType.VOICE -> "Incoming voice call"
                    CallType.VIDEO -> "Incoming video call"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // ---------------- ACTION BUTTONS ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // REJECT
            FloatingActionButton(
                onClick = onReject,
                containerColor = Color.Red
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Reject Call",
                    tint = Color.White
                )
            }

            // ACCEPT
            FloatingActionButton(
                onClick = onAccept,
                containerColor = Color(0xFF2E7D32)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Accept Call",
                    tint = Color.White
                )
            }
        }
    }
}

 */