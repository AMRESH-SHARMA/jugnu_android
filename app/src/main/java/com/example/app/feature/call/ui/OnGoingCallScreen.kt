package com.example.app.feature.call.ui

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.core.rtc.VideoRenderer
import com.example.app.feature.call.domain.CallStatus

@Composable
fun OnGoingCallScreen(
    vm: CallViewModel
) {
    val call by vm.callModel.collectAsState()
    if (call == null) return

    val header by vm.headerUiState.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val remoteUid by vm.remoteUid.collectAsState()

    val status = call!!.status
    val renderer by vm.videoRenderer.collectAsState()

    Box(Modifier.fillMaxSize()) {
        // VIDEO layer only when connected AND remote video present


        if (renderer != null &&
            call!!.callType == CallType.VIDEO &&
            status == CallStatus.CONNECTED
        ) {
            //TODO if remoteuid null show avataar
            VideoArea(renderer!!, remoteUid ?: -1)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                // avatar for voice calls
                if (call!!.callType == CallType.VOICE) {
                    if (header.avatarUrl != null) {
                        AsyncImage(
                            model = header.avatarUrl,
                            contentDescription = "User avatar",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = header.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(6.dp))
                }

                Text(
                    text = when (status) {
                        CallStatus.OUTGOING_RINGING -> "Calling…"
                        CallStatus.CONNECTING -> "Connecting…"
                        CallStatus.CONNECTED -> uiState.durationLabel
                        else -> ""
                    },
                    color = Color.Gray
                )

                Text(
                    text = if (status == CallStatus.CONNECTED)
                        "${uiState.remainingSeconds}s"
                    else "",
                    color = Color.Gray
                )
            }

            CallControls(
                status = status,
                isMuted = uiState.isMuted,
                isSpeakerOn = uiState.isSpeakerOn,
                onToggleMute = vm::toggleMute,
                onToggleSpeaker = vm::toggleSpeaker,
                onEndCall = vm::endCall
            )
        }
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

        IconButton(
            onClick = onToggleMute,
            enabled = status == CallStatus.CONNECTED
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute"
            )
        }

        FloatingActionButton(
            onClick = onEndCall,
            containerColor = Color.Red
        ) {
            Icon(Icons.Default.CallEnd, null, tint = Color.White)
        }

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

@Composable
fun VideoArea(
    renderer: VideoRenderer,
    remoteUid: Int?
) {
    Box(Modifier.fillMaxSize()) {

        // remote
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { SurfaceView(it) },
            update = { view ->
                if (remoteUid != null) {
                    renderer.bindRemote(remoteUid, view)
                }
            }
        )

        // local preview
        AndroidView(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            factory = { context ->
                SurfaceView(context).apply {
                    renderer.bindLocal(this)
                }
            }
        )
    }
}

