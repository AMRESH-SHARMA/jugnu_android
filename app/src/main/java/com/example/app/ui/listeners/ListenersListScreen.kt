package com.example.app.ui.listeners

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.app.ui.components.AvatarWithStatus
import com.example.app.ui.listeners.components.ListenersSearchBar

@Composable
fun ListenerListScreen(
    modifier: Modifier = Modifier,
    onOpenListener: (String) -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }

    val listeners = List(12) { idx -> "Listener item $idx" }

    val filteredListeners = listeners.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // Search Bar
        ListenersSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            hint = "Search listeners"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredListeners) { listener ->
                var showImageDialog by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onOpenListener(listener) },
//                        .clickable { onOpenChat(listener) },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ⭐ Avatar + online indicator
                    AvatarWithStatus(
                        onAvatarClick = { showImageDialog = true }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = listener,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Yesterday",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                val imageScale by animateFloatAsState(
                    targetValue = if (showImageDialog) 1f else 0.8f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "imageScale"
                )

                val imageAlpha by animateFloatAsState(
                    targetValue = if (showImageDialog) 1f else 0f,
                    animationSpec = tween(180),
                    label = "imageAlpha"
                )

                // background alpha separately (optional: smoother control)
                val bgAlpha by animateFloatAsState(
                    targetValue = if (showImageDialog) 0.6f else 0f,
                    animationSpec = tween(180),
                    label = "bgAlpha"
                )

                // ⭐ WhatsApp-style animated popup
                if (showImageDialog) {
                    Dialog(
                        onDismissRequest = { showImageDialog = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = bgAlpha))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { showImageDialog = false })
                                },
                            contentAlignment = Alignment.TopCenter

                        ) {
                            // Zoom + Fade animation applied here
                            Box(
                                modifier = Modifier
                                    .padding(top = 130.dp) // ⭐ shift dialog up/down
                                    .graphicsLayer {
                                        // explicitly reference 'this' to avoid ambiguity
                                        scaleX = imageScale
                                        scaleY = imageScale
                                        this.alpha = imageAlpha
                                    }
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
                                AsyncImage(
                                    model = "https://mdbcdn.b-cdn.net/img/new/avatars/2.webp",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth(0.70f) // ⭐ reduce this % to shrink image and modal
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "last item in the scrollable list.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}