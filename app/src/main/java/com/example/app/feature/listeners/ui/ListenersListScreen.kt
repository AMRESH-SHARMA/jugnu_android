package com.example.app.feature.listeners.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.core.call.CallType
import com.example.app.core.session.SessionManager
import com.example.app.core.websocket.PresenceState
import com.example.app.core.websocket.PresenceViewModel
import com.example.app.feature.call.ui.CallViewModel
import com.example.app.feature.components.AvatarWithStatus
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.ui.components.ListenersSearchBar

// ---------------- MAIN SCREEN ---------------------
@Composable
fun ListenerListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onOpenListener: (ListenerModel) -> Unit = {}
) {
    // ViewModels
    val listenerVm: ListenerViewModel = hiltViewModel()
    val callVm: CallViewModel = hiltViewModel()
    val presenceVm: PresenceViewModel = hiltViewModel()

    // State
    val listeners by listenerVm.listeners.collectAsState()
    val presenceMap by presenceVm.remotePresenceStore.states.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Pending call (ONLY cleared after permission result)
    var pendingCall by remember {
        mutableStateOf<Triple<ListenerModel, CallType, Long>?>(null)
    }

    // Track if permission was ever requested
    var permissionEverRequested by rememberSaveable {
        mutableStateOf(false)
    }

    var showPermissionSettingsDialog by remember {
        mutableStateOf(false)
    }

    // Permission launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                pendingCall?.let { (listener, callType, calleeAccountId) ->
                    callVm.startCall(
                        callType = callType,
                        callerAccountId = SessionManager.userId,
                        calleeAccountId = calleeAccountId,
                        calleeName = listener.name,
                        calleeAvatar = listener.avatar
                    )
                }
            }
            pendingCall = null
        }

    ListenerListContent(
        listeners = listeners,
        presenceMap = presenceMap,
        navController = navController,
        onOpenListener = onOpenListener,
        onCallClick = { listener, callType ->

            val calleeAccountId = listener.accountId ?: return@ListenerListContent

            val requiredPermissions: List<String> = when (callType) {
                CallType.VOICE ->
                    listOf(Manifest.permission.RECORD_AUDIO)

                CallType.VIDEO ->
                    listOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    )
            }

            // 1️⃣ If already granted → start call
            val allGranted = requiredPermissions.all {
                context.hasPermission(it)
            }

            if (allGranted) {
                callVm.startCall(
                    callType = callType,
                    callerAccountId = SessionManager.userId,
                    calleeAccountId = calleeAccountId,
                    calleeName = listener.name,
                    calleeAvatar = listener.avatar
                )
                return@ListenerListContent
            }

            // 2️⃣ Detect TRUE permanent denial
            val permanentlyDenied =
                permissionEverRequested &&
                        requiredPermissions.any { permission ->
                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                permission
                            ) &&
                                    !context.hasPermission(permission)
                        }

            if (permanentlyDenied) {
                // 🚫 Settings only after real permanent denial
                showPermissionSettingsDialog = true
            } else {
                // 🟡 First / temporary denial → show native Android dialog
                permissionEverRequested = true
                pendingCall = Triple(listener, callType, calleeAccountId)
                permissionLauncher.launch(requiredPermissions.toTypedArray())
            }
        }
    )

    // ---------------- SETTINGS DIALOG ---------------------

    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("Permission required") },
            text = {
                Text(
                    "Microphone and Camera access are required to make calls. " +
                            "You can enable them in Settings.", color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionSettingsDialog = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) {
                    Text("Open Settings", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionSettingsDialog = false
                }) {
                    Text("Not now", color = Color.White)
                }
            }
        )
    }
}


// ---------------- REAL CONTENT ---------------------
@Composable
private fun ListenerListContent(
    listeners: List<ListenerModel>,
    presenceMap: Map<String, PresenceState>,
    navController: NavController,
    onOpenListener: (ListenerModel) -> Unit,
    onCallClick: (ListenerModel, CallType) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedListener by remember { mutableStateOf<ListenerModel?>(null) }

    val filtered = listeners.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ListenersSearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            hint = "Search listeners"
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(filtered, key = { it.accountId }) { listener ->

                val status =
                    presenceMap[listener.accountId.toString()] ?: PresenceState.OFFLINE

                ListenerRow(
                    listener = listener,
                    status = status,
                    onOpenListener = onOpenListener,
                    onCallClick = onCallClick,
                    onAvatarClick = {
                        selectedListener = listener
                        showImageDialog = true
                    }
                )
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (showImageDialog && selectedListener != null) {
        ProfilePopupDialog(
            show = true,
            imageUrl = selectedListener!!.avatar,
            rating = "${selectedListener!!.rating}",
            experienceHours = selectedListener!!.experience,
            description = selectedListener!!.about.orEmpty(),
            onDismiss = {
                showImageDialog = false
                selectedListener = null
            }
        )
    }
}


// ---------------- LISTENER ROW---------------------
@Composable
private fun ListenerRow(
    listener: ListenerModel,
    status: PresenceState,
    onOpenListener: (ListenerModel) -> Unit,
    onCallClick: (ListenerModel, CallType) -> Unit,
    onAvatarClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenListener(listener) }
                .padding(horizontal = 2.dp, vertical = 12.dp)
        ) {

            AvatarWithStatus(
                modifier = Modifier.size(80.dp),
                imageUrl = listener.avatar,
                userStatus = status,
                onAvatarClick = onAvatarClick
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(listener.name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                Text("${listener.gender}-${listener.age}")
                Spacer(Modifier.height(6.dp))
                Text("${listener.rating}⭐ (1k+)")
                Text("Exp: ${listener.experience}")
            }

            IconButton(onClick = { onCallClick(listener, CallType.VOICE) }) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null)
            }


            IconButton(onClick = { onCallClick(listener, CallType.VIDEO) }) {
                Icon(imageVector = Icons.Default.VideoCall, contentDescription = null)
            }
        }
    }
}


// ---------------- POPUP DIALOG---------------------
@Composable
fun ProfilePopupDialog(
    show: Boolean,
    imageUrl: String?,
    rating: String,
    experienceHours: Int,
    description: String,
    onDismiss: () -> Unit
) {

    val imageScale by animateFloatAsState(
        targetValue = if (show) 1f else 0.8f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "imageScale"
    )

    val imageAlpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(180),
        label = "imageAlpha"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (show) 0.6f else 0f,
        animationSpec = tween(180),
        label = "bgAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 100.dp)
            ) {

                // ⭐ Image
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            this.alpha = imageAlpha
                        }
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(0.70f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "⭐ $rating",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Experience: $experienceHours hours",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}


// ----------------PERMISSION CHECKER UTILITY---------------------
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
