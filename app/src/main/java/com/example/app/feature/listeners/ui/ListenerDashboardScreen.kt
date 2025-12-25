package com.example.app.feature.listeners.ui


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ---------------------------------------------------
   DATA + RANGE
--------------------------------------------------- */

enum class RevenueRange { DAYS_5, MONTH_1, YEAR_1 }

private val revenueDataMap = mapOf(
    RevenueRange.DAYS_5 to listOf(20f, 40f, 30f, 50f, 60f),
    RevenueRange.MONTH_1 to listOf(
        10f, 15f, 20f, 18f, 25f, 30f, 28f,
        35f, 40f, 38f, 45f, 50f
    ),
    RevenueRange.YEAR_1 to listOf(
        100f, 120f, 110f, 140f, 160f, 180f,
        200f, 220f, 210f, 240f, 260f, 300f
    )
)

private val labelsMap = mapOf(
    RevenueRange.DAYS_5 to listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    RevenueRange.MONTH_1 to listOf("W1", "W2", "W3", "W4"),
    RevenueRange.YEAR_1 to listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
)

/* ---------------------------------------------------
   SCREEN
--------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenerDashboardScreen() {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            HeaderSection("John!")

            Spacer(Modifier.height(20.dp))

            RevenueChartCard()
        }
    }
}

/* ---------------------------------------------------
   HEADER
--------------------------------------------------- */

@Composable
fun HeaderSection(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Good Morning, $username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, CircleShape)
        )
    }
}

/* ---------------------------------------------------
   RANGE SELECTOR
--------------------------------------------------- */

@Composable
fun RevenueRangeSelector(
    selected: RevenueRange,
    onSelect: (RevenueRange) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RevenueRange.values().forEach { range ->
            FilterChip(
                selected = selected == range,
                onClick = { onSelect(range) },
                label = {
                    Text(
                        when (range) {
                            RevenueRange.DAYS_5 -> "5D"
                            RevenueRange.MONTH_1 -> "1M"
                            RevenueRange.YEAR_1 -> "1Y"
                        }
                    )
                }
            )
        }
    }
}

/* ---------------------------------------------------
   CHART CARD
--------------------------------------------------- */

@Composable
fun RevenueChartCard() {
    var selectedRange by remember { mutableStateOf(RevenueRange.DAYS_5) }

    val rawData = revenueDataMap[selectedRange]!!
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        label = "chart_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Revenue", color = Color.White)
                Text("↗ 2%", color = Color.Green)
            }

            Spacer(Modifier.height(12.dp))

            RevenueRangeSelector(
                selected = selectedRange,
                onSelect = { selectedRange = it }
            )

            Spacer(Modifier.height(16.dp))

            RevenueLineChartCurvy(
                data = rawData,
                labels = labelsMap[selectedRange]!!,
                animationProgress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/* ---------------------------------------------------
   CURVY CANVAS CHART + AXIS
--------------------------------------------------- */

@Composable
fun RevenueLineChartCurvy(
    data: List<Float>,
    labels: List<String>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f

    Canvas(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {

        val width = size.width
        val height = size.height * 0.8f
        val stepX = width / (data.size - 1)

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = height - (value / maxValue) * height * animationProgress
            )
        }

        // CURVE
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.lastIndex) {
                val current = points[i]
                val next = points[i + 1]
                val midX = (current.x + next.x) / 2
                cubicTo(
                    midX, current.y,
                    midX, next.y,
                    next.x, next.y
                )
            }
        }

        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // POINTS
        points.forEach {
            drawCircle(Color.Green, radius = 5f, center = it)
        }

        // X-AXIS LABELS
        labels.forEachIndexed { index, label ->
            val x = index * (width / (labels.size - 1))
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                size.height + 30,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}
