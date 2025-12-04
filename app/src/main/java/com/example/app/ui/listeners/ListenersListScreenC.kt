package com.example.app.ui.listeners

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.app.ui.components.AvatarWithStatus
import com.example.app.ui.listeners.components.ListenersSearchBar

@Composable
fun ListenerListScreenC(
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
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredListeners) { listener ->
                var showImageDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .padding(vertical = 12.dp)
                        .clickable { onOpenListener(listener) }
                ) {

                    // ⭐ Avatar + online indicator
                    AvatarWithStatus(
                        modifier = Modifier
                            .size(80.dp),
                        onAvatarClick = { showImageDialog = true }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // ⭐ Middle Column - Flexible area
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = listener,
                            style = MaterialTheme.typography.titleSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Text(
                            "M-20 yrs",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            "5.0⭐ (1k+)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // ⭐ Right Button
                    Column(
                        modifier = Modifier.height(70.dp),
//                        verticalArrangement = Arrangement.Top,
//                        horizontalAlignment = Alignment.End
                    ) {
                        Row {
                            IconButton(
                                onClick = { /* call */ },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call")
                            }

                            IconButton(
                                onClick = { /* video */ },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = "Video Call")
                            }
                        }
                    }
                }

                // ⭐ Animated profile popup
                ProfilePopupDialog(
                    show = showImageDialog,
                    imageUrl = "https://mdbcdn.b-cdn.net/img/new/avatars/2.webp",
                    rating = "4.8 / 5",
                    experienceHours = 120,
                    description = "Very friendly and experienced listener.\nAvailable now for chat.",
                    onDismiss = { showImageDialog = false }
                )


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

@Composable
fun ProfilePopupDialogC(
    show: Boolean,
    imageUrl: String,
    rating: String,
    experienceHours: Int,
    description: String,
    onDismiss: () -> Unit
) {

    // ANIMATIONS
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

    if (show) {
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
                    modifier = Modifier
                        .padding(top = 100.dp)
                ) {

                    // IMAGE
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

                    // ⭐ Rating
                    Text(
                        text = "⭐ $rating",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⭐ Experience in hours
                    Text(
                        text = "Experience: $experienceHours hours",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⭐ Description
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        }
    }
}


//@Composable
//fun ListenerListScreen(
//    modifier: Modifier = Modifier,
//    onOpenListener: (String) -> Unit = {},
//) {
//    var searchQuery by remember { mutableStateOf("") }
//
//    // ⭐ Single source of truth for dialog
//    var showImageDialog by remember { mutableStateOf(false) }
//    var selectedListener by remember { mutableStateOf<String?>(null) }
//
//    val listeners = List(12) { idx -> "Listener item $idx" }
//
//    val filteredListeners = listeners.filter {
//        it.contains(searchQuery, ignoreCase = true)
//    }
//
//    Column(
//        modifier = modifier.fillMaxSize()
//    ) {
//
//        // Search Bar
//        ListenersSearchBar(
//            query = searchQuery,
//            onQueryChange = { searchQuery = it },
//            hint = "Search listeners"
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            items(
//                items = filteredListeners,
//                key = { it } // ⭐ important for scroll performance
//            ) { listener ->
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(20.dp))
//                        .padding(vertical = 12.dp)
//                        .clickable { onOpenListener(listener) }
//                ) {
//
//                    // ⭐ Avatar + online indicator
//                    AvatarWithStatus(
//                        modifier = Modifier.size(80.dp),
//                        onAvatarClick = {
//                            selectedListener = listener
//                            showImageDialog = true
//                        }
//                    )
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    // ⭐ Middle Column
//                    Column(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Spacer(modifier = Modifier.height(5.dp))
//
//                        Text(
//                            text = listener,
//                            style = MaterialTheme.typography.titleSmall,
//                            overflow = TextOverflow.Ellipsis,
//                            maxLines = 1,
//                        )
//
//                        Spacer(modifier = Modifier.height(15.dp))
//
//                        Text(
//                            "M-20 yrs",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//
//                        Spacer(modifier = Modifier.height(5.dp))
//
//                        Text(
//                            "5.0⭐ (1k+)",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//
//                    // ⭐ Right Side: Call + Video buttons
//                    Column(
//                        modifier = Modifier.height(70.dp)
//                    ) {
//                        Row {
//                            IconButton(
//                                onClick = { /* call */ },
//                                colors = IconButtonDefaults.iconButtonColors(
//                                    containerColor = Color.Transparent
//                                )
//                            ) {
//                                Icon(Icons.Default.Call, contentDescription = "Call")
//                            }
//
//                            IconButton(
//                                onClick = { /* video */ },
//                                colors = IconButtonDefaults.iconButtonColors(
//                                    containerColor = Color.Transparent
//                                )
//                            ) {
//                                Icon(Icons.Default.VideoCall, contentDescription = "Video Call")
//                            }
//                        }
//                    }
//                }
//            }
//
//            item {
//                Spacer(modifier = Modifier.height(20.dp))
//                Text(
//                    text = "last item in the scrollable list.",
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(40.dp))
//            }
//        }
//    }
//
//    // ⭐ Show the popup ONCE outside LazyColumn — FIXES SCROLL LAG
//    if (showImageDialog && selectedListener != null) {
//        ProfilePopupDialog(
//            show = true,
//            imageUrl = "https://mdbcdn.b-cdn.net/img/new/avatars/2.webp",
//            rating = "4.8 / 5",
//            experienceHours = 120,
//            description = "Very friendly and experienced listener.\nAvailable now for chat.",
//            onDismiss = {
//                showImageDialog = false
//                selectedListener = null
//            }
//        )
//    }
//}
//
//
//@Composable
//fun ProfilePopupDialog(
//    show: Boolean,
//    imageUrl: String,
//    rating: String,
//    experienceHours: Int,
//    description: String,
//    onDismiss: () -> Unit
//) {
//
//    // ANIMATIONS
//    val imageScale by animateFloatAsState(
//        targetValue = if (show) 1f else 0.8f,
//        animationSpec = tween(220, easing = FastOutSlowInEasing),
//        label = "imageScale"
//    )
//
//    val imageAlpha by animateFloatAsState(
//        targetValue = if (show) 1f else 0f,
//        animationSpec = tween(180),
//        label = "imageAlpha"
//    )
//
//    val bgAlpha by animateFloatAsState(
//        targetValue = if (show) 0.6f else 0f,
//        animationSpec = tween(180),
//        label = "bgAlpha"
//    )
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = bgAlpha))
//                .pointerInput(Unit) {
//                    detectTapGestures(onTap = { onDismiss() })
//                },
//            contentAlignment = Alignment.TopCenter
//        ) {
//
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.padding(top = 100.dp)
//            ) {
//
//                // ⭐ IMAGE
//                Box(
//                    modifier = Modifier
//                        .graphicsLayer {
//                            scaleX = imageScale
//                            scaleY = imageScale
//                            this.alpha = imageAlpha
//                        }
//                        .clip(RoundedCornerShape(20.dp))
//                ) {
//                    AsyncImage(
//                        model = imageUrl,
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .fillMaxWidth(0.70f)
//                            .aspectRatio(1f)
//                            .clip(RoundedCornerShape(20.dp))
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // ⭐ Rating
//                Text(
//                    text = "⭐ $rating",
//                    color = Color.White,
//                    style = MaterialTheme.typography.titleMedium
//                )
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                // ⭐ Experience in hours
//                Text(
//                    text = "Experience: $experienceHours hours",
//                    color = Color.White.copy(alpha = 0.9f),
//                    style = MaterialTheme.typography.bodyMedium
//                )
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                // ⭐ Description
//                Text(
//                    text = description,
//                    color = Color.White.copy(alpha = 0.85f),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.fillMaxWidth(0.8f)
//                )
//            }
//        }
//    }
//}
