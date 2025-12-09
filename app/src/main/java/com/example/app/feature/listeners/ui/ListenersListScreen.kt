package com.example.app.feature.listeners.ui

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.feature.components.AvatarWithStatus
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.ui.components.ListenersSearchBar
import com.example.app.feature.navigation.ui.openOngoingCall

// ---------------- MAIN SCREEN ---------------------------------------

@Composable
fun ListenerListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onOpenListener: (ListenerModel) -> Unit = {},
) {
    val vm: ListenerViewModel = hiltViewModel()

    val data by vm.listeners.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    when {
        loading -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $error")
            }
        }

        else -> {
            ListenerListContent(
                listeners = data,
                navController = navController,
                onOpenListener = onOpenListener
            )
        }
    }
}

// ---------------- REAL CONTENT ---------------------

@Composable
private fun ListenerListContent(
    listeners: List<ListenerModel>,
    navController: NavController,
    onOpenListener: (ListenerModel) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedListenerModel by remember { mutableStateOf<ListenerModel?>(null) }

    val filtered = listeners.filter {
        it.name.contains(searchQuery, true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
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
                            onAvatarClick = {
                                selectedListenerModel = listener
                                showImageDialog = true
                            }
                        )

                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = listener.name,
                                style = MaterialTheme.typography.titleSmall,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${listener.gender}-${listener.age}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "${listener.rating}⭐ (1k+)",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                "Exp: ${listener.experience}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton({
//                            navController.navigate("incoming_call/${listener.id}")
                            navController.openOngoingCall(listener)
                        }) { Icon(Icons.Default.Call, null) }

                        IconButton({
//                            navController.navigate("incoming_call/${listener.id}")
                            navController.openOngoingCall(listener)
                        }) { Icon(Icons.Default.VideoCall, null) }
                    }
                }
            }
// ---------------- FOOTER ---------------------
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

    if (showImageDialog && selectedListenerModel != null) {
        ProfilePopupDialog(
            show = true,
            imageUrl = selectedListenerModel!!.avatar,
            rating = "${selectedListenerModel!!.rating}",
            experienceHours = selectedListenerModel!!.experience,
            description = selectedListenerModel!!.about ?: "",
            onDismiss = {
                showImageDialog = false
                selectedListenerModel = null
            }
        )
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

