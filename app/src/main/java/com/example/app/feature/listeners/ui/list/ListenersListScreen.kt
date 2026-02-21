package com.example.app.feature.listeners.ui.list

import Routes
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState

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
    val listeners = listenerVm.pagedListeners.collectAsLazyPagingItems()
    val presenceMap by presenceVm.remotePresenceStore.states.collectAsState()
    val availabilityMap by presenceVm.remotePresenceStore.availability.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Handle navigation to wallet for insufficient balance
    LaunchedEffect(Unit) {
        callVm.navigateToWallet.collect {
            navController.navigate(Routes.Graph.WALLET) {
                launchSingleTop = true
            }
        }
    }

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
                        callerAccountId = SessionManager.userAccountId,
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
        availabilityMap = availabilityMap,
        navController = navController,
        onOpenListener = onOpenListener,
        onMessageClick = { listener ->
            navController.currentBackStackEntry?.savedStateHandle?.set("listener", listener)
            navController.navigate(Routes.Screen.Chat.chatRoute(listener.accountId ?: 0L))
        },
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
                    callerAccountId = SessionManager.userAccountId,
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
    listeners: LazyPagingItems<ListenerModel>,
    presenceMap: Map<String, PresenceState>,
    availabilityMap: Map<String, Boolean>,
    navController: NavController,
    onOpenListener: (ListenerModel) -> Unit,
    onCallClick: (ListenerModel, CallType) -> Unit,
    onMessageClick: (ListenerModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedListener by remember { mutableStateOf<ListenerModel?>(null) }

//    val presenceVm: PresenceViewModel = hiltViewModel()
//    val presenceMap by presenceVm.remotePresenceStore.states.collectAsState(initial = emptyMap())

//    val filtered = listeners.filter {
//        it.name.contains(searchQuery, ignoreCase = true)
//    }
    // Filtered list for search
//    val filtered = if (searchQuery.isBlank()) listeners else {
//        listeners.filter { it.name.contains(searchQuery, ignoreCase = true) }
//    }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        //TODO
        /*
                ListenersSearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    hint = "Search listeners"
                )
        */
        Spacer(Modifier.height(12.dp))

        when (listeners.loadState.refresh) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return
            }

            is LoadState.Error -> {
                val error = (listeners.loadState.refresh as LoadState.Error).error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to load listeners",
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { listeners.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                return
            }

            else -> Unit
        }
        if (
            listeners.loadState.refresh is LoadState.NotLoading &&
            listeners.itemCount == 0
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No listeners available",
                    color = Color.Gray
                )
            }
            return
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            items(
                count = listeners.itemCount,
                key = { index ->
                    listeners[index]?.accountId ?: "placeholder_$index"
                }
            ) { index ->

                val listener = listeners[index]

                if (listener == null) {
                    ListenerRowPlaceholder()
                    return@items
                }

                // Combine WebSocket data (presence + availability)
                // WebSocket snapshot includes ALL listeners, so no fallback needed
                val wsPresence = presenceMap[listener.accountId.toString()]
                val wsAvailability = availabilityMap[listener.accountId.toString()]
                
                // Use WebSocket data, default to false if not present
                val isAvailable = wsAvailability ?: false
                
                val finalStatus = when {
                    // If listener set silent mode or has no active session, show as unavailable (white dot with X)
                    !isAvailable -> PresenceState.OFFLINE
                    // If listener is on a call, show as busy (red dot)
                    wsPresence == PresenceState.BUSY -> PresenceState.BUSY
                    // If available (has session + wants calls), show as online (green dot)
                    // Even if WebSocket disconnected, they'll get FCM notification
                    else -> PresenceState.ONLINE
                }

                ListenerRow(
                    listener = listener,
                    status = finalStatus,
                    onOpenListener = onOpenListener,
                    onCallClick = onCallClick,
                    onMessageClick = onMessageClick,
                    onAvatarClick = {
                        selectedListener = listener
                        showImageDialog = true
                    }
                )
            }

            // ---- APPEND LOADING ----
            if (listeners.loadState.append is LoadState.Loading) {
                item(key = "append_loader") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ---- APPEND ERROR ----
            if (listeners.loadState.append is LoadState.Error) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Failed to load more",
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { listeners.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
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
fun ListenerRowPlaceholder() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
//            .padding(horizontal = 2.dp, vertical = 6.dp)
            .heightIn(min = 104.dp) // lock height
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Gray.copy(alpha = 0.25f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {

                // Name placeholder
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.25f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rating placeholder
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Experience placeholder
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Call icon placeholders
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
        }
    }
}
@Composable
private fun ListenerRow(
    listener: ListenerModel,
    status: PresenceState,
    onOpenListener: (ListenerModel) -> Unit,
    onCallClick: (ListenerModel, CallType) -> Unit,
    onAvatarClick: () -> Unit,
    onMessageClick: (ListenerModel) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = listener.name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "${listener.gender} • ${listener.age}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("${listener.rating}⭐ (1k+)", style = MaterialTheme.typography.bodySmall)
                Text("Exp: ${listener.experience}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = { onMessageClick(listener) }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Message,
                    contentDescription = "Message",
                    tint = Color.White
                )
            }

            IconButton(onClick = { onCallClick(listener, CallType.VOICE) }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Audio Call",
                    tint = Color.White
                )
            }

            IconButton(onClick = { onCallClick(listener, CallType.VIDEO) }) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = "Video Call",
                    tint = Color.White
                )
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
                        // TODO
//                        placeholder = painterResource(R.drawable.ic_avatar_placeholder),
//                        error = painterResource(R.drawable.ic_avatar_placeholder),
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
