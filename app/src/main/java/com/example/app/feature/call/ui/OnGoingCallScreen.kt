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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.core.rtc.AgoraVideoRtcManager
import com.example.app.feature.call.domain.CallStatus

@Composable
fun OnGoingCallScreen(
    vm: CallViewModel
) {
    // 🔥 Observe only what is needed
    val call by vm.callModel.collectAsState()
    val header by vm.headerUiState.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val status by vm.callStatus.collectAsState()

    if (call == null || status == null) return

    // 🔑 Ask VM for the current RTC manager (do NOT create one here)
    val rtcManager = vm.currentRtcManager()

    Box(modifier = Modifier.fillMaxSize()) {

        // ------------------------------------------------------------
        // 🎥 VIDEO LAYER (only when VIDEO call + connected)
        // ------------------------------------------------------------
        if (
            call!!.callType == CallType.VIDEO &&
            status == CallStatus.CONNECTED &&
            rtcManager is AgoraVideoRtcManager
        ) {
            VideoArea(
                videoRtcManager = rtcManager,
                remoteUid = vm.remoteUid.collectAsState().value
            )
        }


        // ------------------------------------------------------------
        // 🔊 VOICE / OVERLAY UI (always on top)
        // ------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // --------------------------------------------------------
            // TOP — USER INFO + STATUS (for VOICE or as overlay on VIDEO)
            // --------------------------------------------------------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Avatar (hide for video if you want later)
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
                            modifier = Modifier
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = when (status) {
                        CallStatus.CONNECTED -> "${uiState.remainingSeconds}s"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // --------------------------------------------------------
            // BOTTOM — CONTROLS
            // --------------------------------------------------------
            CallControls(
                status = status!!,
                isMuted = uiState.isMuted,
                isSpeakerOn = uiState.isSpeakerOn,
                onToggleMute = vm::toggleMute,
                onToggleSpeaker = vm::toggleSpeaker,
                onEndCall = vm::endCall
            )
        }
    }
}

//@Composable
//fun OnGoingCallScreen(
//    vm: CallViewModel
//) {
//    // 🔥 Observe only what is needed
//    val call by vm.callModel.collectAsState()
//    val header by vm.headerUiState.collectAsState()
//    val uiState by vm.uiState.collectAsState()
//    val status by vm.callStatus.collectAsState()
//
//    if (call.callType == CallType.VIDEO && status == CallStatus.CONNECTED) {
//        VideoArea(
//            videoRtcManager = vm.getVideoRtcManager()
//        )
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        // ----------------------------------------------------------------
//        // TOP — USER INFO + STATUS
//        // ----------------------------------------------------------------
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//
//            // Avatar
//            if (header.avatarUrl != null) {
//                AsyncImage(
//                    model = header.avatarUrl,
//                    contentDescription = "User avatar",
//                    modifier = Modifier
//                        .size(96.dp)
//                        .clip(CircleShape)
//                )
//            } else {
//                Box(
//                    modifier = Modifier
//                        .size(96.dp)
//                        .clip(CircleShape)
//                        .background(Color.Gray.copy(alpha = 0.3f))
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            // Name
//            Text(
//                text = header.name,
//                style = MaterialTheme.typography.titleLarge
//            )
//
//            Spacer(Modifier.height(6.dp))
//
//            // Status text
//            Text(
//                text = when (status) {
//                    CallStatus.OUTGOING_RINGING -> "Calling…"
//                    CallStatus.CONNECTING -> "Connecting…"
//                    CallStatus.CONNECTED -> uiState.durationLabel
//                    else -> ""
//                },
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray
//            )
//        }
//
//        // ----------------------------------------------------------------
//        // BOTTOM — CONTROLS
//        // ----------------------------------------------------------------
//        CallControls(
//            status = status!!,
//            isMuted = uiState.isMuted,
//            isSpeakerOn = uiState.isSpeakerOn,
//            onToggleMute = vm::toggleMute,
//            onToggleSpeaker = vm::toggleSpeaker,
//            onEndCall = vm::endCall
//        )
//    }
//}


@Composable
private fun CallHeader(
    header: CallHeaderUiState,
    status: CallStatus,
    duration: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Avatar
        if (header.avatarUrl != null) {
            AsyncImage(
                model = header.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Name
        Text(
            text = header.name.ifEmpty { "Connecting…" },
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(6.dp))

        // Subtitle / Status
        when (status) {
            CallStatus.OUTGOING_RINGING -> Text("Calling…")
            CallStatus.CONNECTING -> Text("Connecting…")
            CallStatus.CONNECTED -> Text(duration)
            else -> Unit
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
    videoRtcManager: AgoraVideoRtcManager,
    remoteUid: Int?
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 🔵 Remote video
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                SurfaceView(context)
            },
            update = { view ->
                if (remoteUid != null) {
                    videoRtcManager.setupRemoteVideo(remoteUid, view)
                }
            }
        )

        // 🟢 Local preview (small overlay)
        AndroidView(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            factory = { context ->
                SurfaceView(context).apply {
                    videoRtcManager.setupLocalVideo(this)
                }
            }
        )
    }
}

