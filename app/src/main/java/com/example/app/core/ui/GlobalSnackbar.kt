package com.example.app.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun GlobalSnackbar() {
    val snackbarData by SnackbarManager.snackbarState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(Float.MAX_VALUE), // Highest z-index
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = snackbarData != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            snackbarData?.let { data ->
                // Add top padding for system status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding()
                        )
                ) {
                    SnackbarContent(data)
                }

                // Auto-dismiss after duration
                LaunchedEffect(data) {
                    kotlinx.coroutines.delay(data.duration)
                    SnackbarManager.dismiss()
                }
            }
        }
    }
}

@Composable
private fun SnackbarContent(data: SnackbarData) {
    val (backgroundColor, iconColor, icon) = when (data.type) {
        SnackbarType.SUCCESS -> Triple(
            Color(0xFF1B5E20),  // Dark green
            Color(0xFF4CAF50),  // Bright green for icon
            Icons.Default.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            Color(0xFFB71C1C),  // Dark red
            Color(0xFFF44336),  // Bright red for icon
            Icons.Default.Error
        )
        SnackbarType.WARNING -> Triple(
            Color(0xFF6D4C00),  // Dark yellow/amber
            Color(0xFFFFC107),  // Bright yellow for icon
            Icons.Default.Warning
        )
        SnackbarType.INFO -> Triple(
            Color(0xFF0D47A1),  // Dark blue
            Color(0xFF2196F3),  // Bright blue for icon
            Icons.Default.Info
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = data.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
