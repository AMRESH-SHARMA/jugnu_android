package com.example.app.feature.call.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun IncomingCallBanner(
    vm: CallViewModel,
    modifier: Modifier = Modifier,
) {
    val call by vm.callModel.collectAsState()
    val header by vm.headerUiState.collectAsState()

    // Only show when there is actually an incoming call
    if (call == null || call!!.status != CallStatus.INCOMING_RINGING) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // --------- LEFT SIDE (name + avatar) ---------
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (header.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    AsyncImage(
                        model = header.avatarUrl,
                        contentDescription = "Caller Avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = header.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = when (call!!.callType) {
                            CallType.VOICE -> "Incoming voice call"
                            CallType.VIDEO -> "Incoming video call"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // --------- RIGHT SIDE (buttons) ---------
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                IconButton(
                    onClick = vm::rejectCall,
                    modifier = Modifier
                        .background(Color(0xFFE53935), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = vm::acceptCall,
                    modifier = Modifier
                        .background(Color(0xFF2E7D32), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Accept",
                        tint = Color.White
                    )
                }
            }

        }
    }
}
