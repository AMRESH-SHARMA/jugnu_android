package com.example.app.feature.listenerDashboard.ui

import android.graphics.Paint
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app.feature.listenerDashboard.ui.components.ListenerBottomTabBar
import com.example.app.feature.listenerDashboard.ui.components.ListenerTab

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
fun ListenerDashboardScreen(navController: NavHostController) {

    var selectedTab by remember { mutableStateOf(ListenerTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            ListenerBottomTabBar(
                selected = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {

                ListenerTab.DASHBOARD -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        HeaderSection(username = "John!")

                        Spacer(Modifier.height(20.dp))

                        OverviewCards()

                        Spacer(Modifier.height(20.dp))

                        RevenueChartCard()

                        Spacer(Modifier.height(20.dp))

                        TasksCard()

                        Spacer(Modifier.height(20.dp))

                        PopularProducts()
                    }
                }

                ListenerTab.CALLS -> {
                    // TODO: calls list screen
                }

                ListenerTab.SETTINGS -> {
                    // TODO: reuse settings
                    // SettingsScreen(navController)
                }
            }
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
                Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}


/* ---------------------------------------------------
   OVERVIEW CARDS
--------------------------------------------------- */
@Composable
fun OverviewCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OverviewCard("Total Sales", "$4,200", "+1.5%")
        OverviewCard("Total Visitors", "18,729", "+0.3%")
    }
}

@Composable
fun OverviewCard(title: String, value: String, change: String) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Text(value, color = MaterialTheme.colorScheme.onSurface)
            Text(change, color = Color.Green, fontSize = 12.sp)
        }
    }
}


/* ---------------------------------------------------
   TASK CARDS
--------------------------------------------------- */
@Composable
fun TasksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tasks Done", color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.6f,
                color = Color(0xFFFF8000)
            )
        }
    }
}

/* ---------------------------------------------------
   OVERVIEW CARDS
--------------------------------------------------- */
@Composable
fun PopularProducts() {
    Column {
        Text("Popular Products", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        ProductCard("Creative Bag", "Available", "14k views")
        ProductCard("Electric Mug", "Available", "8k views")
    }
}

@Composable
fun ProductCard(name: String, status: String, views: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Medium)
                Text(views, color = Color.Gray, fontSize = 12.sp)
            }

            Text(status, color = Color.Green, fontSize = 12.sp)
        }
    }
}