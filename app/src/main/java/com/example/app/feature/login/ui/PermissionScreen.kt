package com.example.app.feature.login.ui

import Routes
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.core.session.SessionManager
import com.example.app.feature.components.HeadingTextComponent

@Composable
fun PermissionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var shouldShowRationale by remember { mutableStateOf(false) }
    
    // Disable back button - this is a blocking screen
    BackHandler(enabled = true) {
        // Do nothing - prevent back navigation
    }
    
    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        } else {
            // Permission granted, navigate to next screen
            navigateNext(navController)
        }
    }
    
    // Check permission status
    fun checkPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            // Check if we should show rationale
            if (!notificationPermissionGranted && context is android.app.Activity) {
                shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        } else {
            // Auto-granted on Android 12 and below
            notificationPermissionGranted = true
        }
    }
    
    // Check permission on launch
    LaunchedEffect(Unit) {
        checkPermissionStatus()
        // If already granted, navigate immediately
        if (notificationPermissionGranted) {
            navigateNext(navController)
        }
    }
    
    // Re-check permission when app comes to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissionStatus()
                // Dismiss dialog and navigate if permission is now granted
                if (notificationPermissionGranted) {
                    showPermissionDialog = false
                    navigateNext(navController)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            HeadingTextComponent("Enable Notifications")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Stay connected and never miss important updates",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Benefits list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionBenefit(
                    icon = "📞",
                    title = "Receive Calls",
                    description = "Get notified when someone calls you"
                )
                
                PermissionBenefit(
                    icon = "💬",
                    title = "New Messages",
                    description = "Stay updated with instant message alerts"
                )
                
                PermissionBenefit(
                    icon = "🔔",
                    title = "Important Updates",
                    description = "Don't miss any important notifications"
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Grant Permission Button
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Auto-granted, navigate
                        navigateNext(navController)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Enable Notifications",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "This permission is required to use the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Permission Required Dialog (when denied)
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss - blocking */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (shouldShowRationale) {
                        "Notification permission is required to use this app. Without it, you won't receive calls or messages. Please grant permission to continue."
                    } else {
                        "Notification permission is required. Please enable it in your device settings:\n\nSettings → Apps → ${context.packageManager.getApplicationLabel(context.applicationInfo)} → Permissions → Notifications"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shouldShowRationale) {
                            // User can still be prompted
                            showPermissionDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // User selected "Don't ask again" - open app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (shouldShowRationale) "Grant Permission" else "Open Settings",
                        color = Color.White
                    )
                }
            }
        )
    }
}

@Composable
private fun PermissionBenefit(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun navigateNext(navController: NavController) {
    // Check if customer needs profile setup
    if (SessionManager.userRole == UserRole.CUSTOMER && !SessionManager.isProfileComplete) {
        navController.navigate(Routes.Screen.Auth.PROFILE_SETUP) {
            popUpTo(Routes.Screen.Auth.PERMISSION) { inclusive = true }
            launchSingleTop = true
        }
    } else {
        // Navigate to home/dashboard
        val destination = if (SessionManager.userRole == UserRole.LISTENER) {
            Routes.Graph.LISTENER
        } else {
            Routes.Graph.HOME
        }
        
        navController.navigate(destination) {
            popUpTo(Routes.Graph.AUTH) { inclusive = true }
            launchSingleTop = true
        }
    }
}
