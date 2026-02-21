package com.example.app.feature.call.ui

import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.core.rtc.VideoRenderer
import com.example.app.feature.call.domain.CallStatus
import kotlin.math.roundToInt

@Composable
@UiComposable
fun OnGoingCallScreen(
    vm: CallViewModel
) {
    val context = LocalContext.current
    
    // Disable back button during call
    BackHandler(enabled = true) {
        // Do nothing - prevent back navigation during call
    }

    val call by vm.callModel.collectAsState()
    if (call == null) return

    val header by vm.headerUiState.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val remoteUid by vm.remoteUid.collectAsState()

    val status = call!!.status
    val renderer by vm.videoRenderer.collectAsState()
    
    // Permission check state
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionCheckDone by remember { mutableStateOf(false) }
    
    // Check permissions when screen opens (especially from notification)
    LaunchedEffect(call!!.callId) {
        if (!permissionCheckDone) {
            permissionCheckDone = true
            
            // Check if we have required permissions
            val requiredPermissions = if (call!!.callType == CallType.VOICE) {
                listOf(android.Manifest.permission.RECORD_AUDIO)
            } else {
                listOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.CAMERA
                )
            }
            
            val hasAllPermissions = requiredPermissions.all { permission ->
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            
            // If permissions are missing and call is incoming, show dialog
            if (!hasAllPermissions && status == CallStatus.INCOMING_RINGING) {
                showPermissionDialog = true
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // VIDEO layer only when connected AND remote video present

//        if (renderer != null &&
//            call!!.callType == CallType.VIDEO &&
//            status == CallStatus.CONNECTED
//        ) {
//            //TODO if remoteuid null show avataar
//            VideoArea(renderer!!, remoteUid ?: -1)
//        }
        if (renderer != null && call!!.callType == CallType.VIDEO) {
            VideoArea(renderer!!, remoteUid)
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 96.dp)
            ) {

                // avatar for voice calls
                if (call!!.callType == CallType.VOICE) {
                    key(header.avatarUrl, header.name) {
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
                }

                Text(
                    text = remember(status, remoteUid, uiState.durationLabel) {
                        when (status) {
                            CallStatus.OUTGOING_CONNECTING -> "Connecting…"
                            CallStatus.OUTGOING_RINGING -> "Ringing…"
                            CallStatus.CONNECTING -> "Connecting…"
                            CallStatus.CONNECTED ->
                                if (remoteUid != null) uiState.durationLabel else "Connecting…"

                            else -> ""
                        }
                    },
                    color = Color.Gray
                )
                //TODO: Remaining Seconds
//                Text(
//                    text = if (status == CallStatus.CONNECTED)
//                        "${uiState.remainingSeconds}s"
//                    else "",
//                    color = Color.Gray
//                )
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
    
    // Show permission dialog if needed
    if (showPermissionDialog) {
        CallPermissionDialog(
            callType = call!!.callType,
            onPermissionsGranted = {
                showPermissionDialog = false
                // Accept the call now that we have permissions
                vm.acceptCall()
            },
            onDismiss = {
                showPermissionDialog = false
                // Reject the call if user doesn't grant permission
                vm.rejectCall()
            }
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

        IconButton(
            onClick = onToggleMute,
            enabled = status == CallStatus.CONNECTED,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        FloatingActionButton(
            onClick = onEndCall,
            containerColor = Color.Red
        ) {
            Icon(
                Icons.Default.CallEnd,
                null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = onToggleSpeaker,
            enabled = status == CallStatus.CONNECTED,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = "Speaker",
                tint = if (isSpeakerOn) Color.Green else Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

    }
}

@Composable
@UiComposable
fun VideoArea(
    renderer: VideoRenderer,
    remoteUid: Int?
) {
    val context = LocalContext.current

    // UI state for camera (front/back icon)
    var isFrontCamera by remember { mutableStateOf(true) }

    // drag state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxSize()) {

        /* ---------------- REMOTE VIDEO ---------------- */
        val remoteView = remember { SurfaceView(context) }

        LaunchedEffect(remoteUid) {
            if (remoteUid != null) {
                renderer.bindRemote(remoteUid, remoteView)
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { remoteView }
        )

        /* ---------------- LOCAL PREVIEW ---------------- */
        val localView = remember { SurfaceView(context) }

        LaunchedEffect(Unit) {
            renderer.bindLocal(localView)
        }

        Box(
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(3f / 4f)
                .align(Alignment.BottomEnd)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        offsetX += drag.x
                        offsetY += drag.y
                    }
                }
                .padding(bottom = 88.dp, end = 16.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { localView }
            )

            /* -------- SWITCH CAMERA BUTTON -------- */

            IconButton(
                onClick = {
                    renderer.switchCamera()
                    isFrontCamera = !isFrontCamera
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cached, // camera flip icon
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }
        }
    }
}