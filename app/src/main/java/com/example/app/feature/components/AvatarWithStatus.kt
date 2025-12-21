package com.example.app.feature.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.core.websocket.PresenceState
import com.example.app.feature.theme.USER_STATUS_BUSY
import com.example.app.feature.theme.USER_STATUS_OFFLINE
import com.example.app.feature.theme.USER_STATUS_ONLINE

@Composable
fun AvatarWithStatus(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    userStatus: PresenceState,
    onAvatarClick: () -> Unit = {}
) {
    val statusColor = when (userStatus) {
        PresenceState.ONLINE -> USER_STATUS_ONLINE
        PresenceState.BUSY -> USER_STATUS_BUSY
        PresenceState.OFFLINE -> USER_STATUS_OFFLINE
    }

    // animate dot transition
    val animatedColor by animateColorAsState(statusColor, label = "statusColor")

    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAvatarClick() }
        )

        Box(
            Modifier
                .size(14.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(animatedColor)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}