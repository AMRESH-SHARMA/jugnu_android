package com.example.app.feature.call.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import com.example.app.core.call.CallType

@Composable
fun CallPermissionDialog(
    callType: CallType,
    onPermissionsGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    
    // Required permissions based on call type
    val requiredPermissions = if (callType == CallType.VOICE) {
        listOf(Manifest.permission.RECORD_AUDIO)
    } else {
        listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            onPermissionsGranted()
        } else {
            // Check if permanently denied
            val permanentlyDenied = activity?.let { act ->
                requiredPermissions.any { permission ->
                    !ActivityCompat.shouldShowRequestPermissionRationale(act, permission) &&
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                }
            } ?: false
            
            if (permanentlyDenied) {
                showSettingsDialog = true
            } else {
                // User denied but can be asked again
                onDismiss()
            }
        }
    }
    
    // Auto-request permission on first show
    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }
    
    // Settings dialog (when permanently denied)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = "Permission Required",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                val permissionText = if (callType == CallType.VOICE) {
                    "Microphone permission is required to answer voice calls."
                } else {
                    "Microphone and Camera permissions are required to answer video calls."
                }
                
                Text(
                    text = "$permissionText\n\nPlease enable it in your device settings:\n\nSettings → Apps → ${context.packageManager.getApplicationLabel(context.applicationInfo)} → Permissions",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Open Settings",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White
                    )
                }
            }
        )
    }
}
