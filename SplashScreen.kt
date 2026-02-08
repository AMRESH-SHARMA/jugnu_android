package com.example.app.root

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun SplashScreen(
    onTimeout: () -> Unit = {}
) {
    // Track if timeout occurred
    LaunchedEffect(Unit) {
        delay(10000) // 10 second timeout
        onTimeout()
    }

    // Ultra-premium gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27), // Deep navy
                        Color(0xFF1A1F3A), // Rich midnight blue
                        Color(0xFF0F1419)  // Almost black
                    )
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Animated particles background
        PremiumParticlesBackground()
        
        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Animated glow effect behind text
            AnimatedGlowCircle()
            
            // Jugnu text with premium styling
            Text(
                text = "Jugnu",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Tagline
            Text(
                text = "Connect • Listen • Heal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Premium loading indicator
            PremiumLoadingIndicator()
        }
    }
}

@Composable
fun AnimatedGlowCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Canvas(modifier = Modifier.size(200.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = alpha),
                    Color(0xFFFFA500).copy(alpha = alpha * 0.5f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / 2 * scale
        )
    }
}

@Composable
fun PremiumLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "rotation"
    )
    
    Canvas(modifier = Modifier.size(40.dp)) {
        val strokeWidth = 3.dp.toPx()
        
        // Outer arc
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFA500),
                    Color(0xFFFF8C00),
                    Color.Transparent
                )
            ),
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun PremiumParticlesBackground() {
    val particles = remember {
        List(40) { index ->
            Firefly(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.3f + 0.2f,
                size = Random.nextFloat() * 2f + 1f,
                angle = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val animatedPositions = particles.map { particle ->
        val animX = remember { Animatable(particle.startX) }
        val animY = remember { Animatable(particle.startY) }
        
        LaunchedEffect(particle.id) {
            while (true) {
                val duration = (4000 / particle.speed).toInt()
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
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEachIndexed { index, particle ->
            val (animX, animY) = animatedPositions[index]
            val x = animX.value * width
            val y = animY.value * height

            // Glow effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = glowAlpha * 0.4f),
                        Color(0xFFFFA500).copy(alpha = glowAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = particle.size * 12
                ),
                radius = particle.size * 12,
                center = Offset(x, y)
            )

            // Core particle
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

