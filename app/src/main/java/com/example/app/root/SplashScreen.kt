package com.example.app.root

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Firefly(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val angle: Float
)

@Composable
fun SplashScreen() {
    // Netflix-style black background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Fireflies animation in background
        FirefliesBackground()
        
        // Jugnu text (Netflix-style)
        Text(
            text = "Jugnu",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFBBF24), // Golden/amber color for firefly theme
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun FirefliesBackground() {
    val fireflies = remember {
        List(25) { index ->
            Firefly(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.3f,
                size = Random.nextFloat() * 4f + 2f,
                angle = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fireflies")
    
    val animatedPositions = fireflies.map { firefly ->
        val animX = remember { Animatable(firefly.startX) }
        val animY = remember { Animatable(firefly.startY) }
        
        LaunchedEffect(firefly.id) {
            while (true) {
                val duration = (3000 / firefly.speed).toInt()
                val targetX = Random.nextFloat()
                val targetY = Random.nextFloat()
                
                animX.animateTo(
                    targetValue = targetX,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
                animY.animateTo(
                    targetValue = targetY,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
                delay(100)
            }
        }
        
        animX to animY
    }

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        fireflies.forEachIndexed { index, firefly ->
            val (animX, animY) = animatedPositions[index]
            val x = animX.value * width
            val y = animY.value * height

            // Glow effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBBF24).copy(alpha = glowAlpha * 0.6f),
                        Color(0xFFFBBF24).copy(alpha = 0f)
                    ),
                    center = Offset(x, y),
                    radius = firefly.size * 8
                ),
                radius = firefly.size * 8,
                center = Offset(x, y)
            )

            // Core firefly
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = glowAlpha),
                radius = firefly.size,
                center = Offset(x, y)
            )
        }
    }
}

