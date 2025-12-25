package com.example.app.feature.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.core.websocket.PresenceState
import com.example.app.core.websocket.PresenceViewModel
import com.example.app.feature.components.AvatarWithStatus
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.ui.list.ProfilePopupDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    listenerModel: ListenerModel?,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val presenceVm: PresenceViewModel = hiltViewModel()
    val presenceMap by presenceVm.remotePresenceStore.states.collectAsState()

    val status = listenerModel?.accountId?.toString()?.let {
        presenceMap[it] ?: PresenceState.OFFLINE
    } ?: PresenceState.OFFLINE

    // ⭐ State for opening the image modal (same as listener screen)
    var showImageDialog by remember { mutableStateOf(false) }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        },
        title = {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // LEFT SIDE — Avatar + Title (clickable together)
                Row(
                    modifier = Modifier.clickable {
                        showImageDialog = true
                    },   // ⭐ tap to open modal
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AvatarWithStatus(
                        modifier = Modifier.size(40.dp),
                        imageUrl = listenerModel?.avatar ?: "",
                        userStatus = status,
                        onAvatarClick = { showImageDialog = true }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = listenerModel?.name ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }

                // RIGHT SIDE — Call + Video buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVoiceCall) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }

                    IconButton(onClick = onVideoCall) {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = "Video Call",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            titleContentColor = MaterialTheme.colorScheme.onTertiary
        ),
        scrollBehavior = scrollBehavior
    )

    // ⭐ Reuse your profile popup dialog (exact same component)
    if (showImageDialog) {
        ProfilePopupDialog(
            show = true,
            imageUrl = listenerModel?.avatar,
            rating = "4.8 / 5",
            experienceHours = 120,
            description = "Very friendly and experienced listener.\nAvailable now for chat.",
            onDismiss = { showImageDialog = false }
        )
    }
}