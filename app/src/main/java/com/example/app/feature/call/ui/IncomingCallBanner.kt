package com.example.app.feature.call.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.feature.call.domain.CallStatus

@Composable
fun IncomingCallBanner(
    vm: CallViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as Activity

    val call by vm.callModel.collectAsState()
    val header by vm.headerUiState.collectAsState()

    var pendingAccept by remember { mutableStateOf(false) }
    var permissionEverRequested by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // ------------------------------------------------------------
    // Permission launcher
    // ------------------------------------------------------------
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            val allGranted = result.values.all { it }

            if (allGranted && pendingAccept) {
                vm.acceptCall()
            }

            pendingAccept = false
        }

    // ------------------------------------------------------------
    // Only show when there is actually an incoming call
    // ------------------------------------------------------------
    if (call == null || call!!.status != CallStatus.INCOMING_RINGING) return

    // Which permissions are required?
    val requiredPermissions =
        if (call!!.callType == CallType.VOICE)
            listOf(Manifest.permission.RECORD_AUDIO)
        else
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )

    fun handleAcceptClick() {

        val allGranted = requiredPermissions.all { context.hasPermission(it) }

        if (allGranted) {
            vm.acceptCall()
            return
        }

        val permanentlyDenied =
            permissionEverRequested &&
                    requiredPermissions.any {
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) &&
                                !context.hasPermission(it)
                    }

        if (permanentlyDenied) {
            showSettingsDialog = true
        } else {
            permissionEverRequested = true
            pendingAccept = true
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // ------------------------------------------------------------
    // UI
    // ------------------------------------------------------------
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
                    onClick = { handleAcceptClick() },
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

    // ------------------------------------------------------------
    // SETTINGS DIALOG (permanently denied)
    // ------------------------------------------------------------
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission needed") },
            text = { Text("Microphone (and Camera, if video) are required to answer calls.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    openAppSettings(context)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}