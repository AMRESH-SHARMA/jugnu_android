package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.ui.theme.STATUS_ACTIVE

@Composable
fun AvatarWithStatus(
    modifier: Modifier,
    imageUrl: String?,
    onAvatarClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAvatarClick() }
        )

        // Status dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(STATUS_ACTIVE)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}