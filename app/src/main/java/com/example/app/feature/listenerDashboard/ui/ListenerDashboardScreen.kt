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
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.core.ui.UiState
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import com.example.app.feature.listenerDashboard.ui.components.ListenerBottomTabBar
import com.example.app.feature.listenerDashboard.ui.components.ListenerTab
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

enum class StatsFilter { ALL_TIME, DAYS_30, CUSTOM }

/* ---------------------------------------------------
   SCREEN
--------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenerDashboardScreen(
    navController: NavHostController,
    onWalletClick: () -> Unit = {},
    vm: ListenerDashboardViewModel = hiltViewModel()
) {
    val uiState by vm.stats.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    var selectedTab by remember { mutableStateOf(ListenerTab.DASHBOARD) }

    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDateRangePickerState()

    var activeFilter by remember { mutableStateOf(StatsFilter.ALL_TIME) }

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
                    when (uiState) {
                        is UiState.Success -> {
                            val stats = (uiState as UiState.Success).data
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                HeaderSection(username = stats.name, avatarUrl = stats.avatar)

                                Spacer(Modifier.height(20.dp))

                                // ---------- FILTER ROW ----------
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // ALL TIME
                                    FilterChip(
                                        selected = activeFilter == StatsFilter.ALL_TIME,
                                        onClick = {
                                            if (activeFilter != StatsFilter.ALL_TIME) {   // 👈 only if changed
                                                activeFilter = StatsFilter.ALL_TIME
                                                vm.setDateRange(null, null)
                                                vm.load()
                                            }
                                        },
                                        label = {
                                            Text(
                                                "All time",
                                                color = MaterialTheme.colorScheme.onTertiary
                                            )
                                        }
                                    )

                                    // LAST 30 DAYS
                                    FilterChip(
                                        selected = activeFilter == StatsFilter.DAYS_30,
                                        onClick = {
                                            if (activeFilter != StatsFilter.DAYS_30) {   // 👈 only if changed
                                                activeFilter = StatsFilter.DAYS_30

                                                val today = LocalDate.now()
                                                val from = today.minusDays(30).toString()
                                                val to = today.toString()

                                                vm.setDateRange(from, to)
                                                vm.load()
                                            }
                                        },
                                        label = {
                                            Text(
                                                "Last 30 days",
                                                color = MaterialTheme.colorScheme.onTertiary
                                            )
                                        }
                                    )

                                    // TODO
                                    // CUSTOM
//                            FilterChip(
//                                selected = activeFilter == StatsFilter.CUSTOM,
//                                onClick = { showPicker = true },
//                                label = { Text("Custom") }
//                            )
                                }

                                Spacer(Modifier.height(20.dp))

                                OverviewCards(stats)
                                Spacer(Modifier.height(20.dp))
                                //TODO
//                        RevenueChartCard()
//                        Spacer(Modifier.height(20.dp))

                                TasksCard()
                                Spacer(Modifier.height(20.dp))

                                StatRows(stats)
                            }
                        }

                        is UiState.Error -> Unit
                        else -> {}
                    }
                }

                ListenerTab.CALLS -> {
                    // TODO
                }

                ListenerTab.SETTINGS -> {
                    com.example.app.feature.user.ui.UserInfoScreen(
                        onWalletClick = {
                            navController.navigate(Routes.Graph.WALLET) {
                                launchSingleTop = true
                            }
                        },
                        onLogout = {
                            navController.navigate(Routes.Graph.AUTH) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // ---------- DATE RANGE PICKER ----------
            if (showPicker) {
                DatePickerDialog(
                    onDismissRequest = { showPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val start = pickerState.selectedStartDateMillis
                                val end = pickerState.selectedEndDateMillis

                                if (start != null && end != null) {

                                    fun Long.toDate(): String =
                                        Instant.ofEpochMilli(this)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                            .toString()

                                    vm.setDateRange(
                                        start.toDate(),
                                        end.toDate()
                                    )

                                    activeFilter = StatsFilter.CUSTOM
                                    vm.load()
                                    showPicker = false
                                }
                            }
                        ) {
                            Text(
                                "Apply",
                                Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                ) {
                    DateRangePicker(
                        state = pickerState,
                        title = {},          // 👈 hides "Select dates heading"
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedDayContainerColor = Color(0xFF00C853),      // green circle
                            selectedDayContentColor = Color.White,              // white text
                            dayInSelectionRangeContainerColor = Color(0x3300C853), // light green range fill
                            dayInSelectionRangeContentColor = Color.White,

                            // 👇 TODAY styling
                            todayContentColor = Color.White,                 // today's number
                            todayDateBorderColor = Color(0xFF00C853)         // green circle ring
                        ),
                    )
                }
            }

        }
    }
}

/* ---------------------------------------------------
   HEADER
--------------------------------------------------- */

@Composable
fun HeaderSection(username: String, avatarUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Hello, $username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        AsyncImage(
            model = avatarUrl,
            contentDescription = "Caller Avatar",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
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
        RevenueRange.entries.forEach { range ->
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
fun OverviewCards(stats: ListenerStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OverviewCard("Total Callers", stats.uniqueCallers.toString(), "+1.5%")
        OverviewCard("Net Earnings", "₹${stats.netEarnings}", "+0.3%")
        //TODO
//        OverviewCard("Ratings", stats.totalRatings.toString(), "")
//        OverviewCard("Reviews", stats.totalReviews.toString(), "")
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
            Text(title, color = MaterialTheme.colorScheme.onTertiary)
            Text(value, color = MaterialTheme.colorScheme.onTertiary)
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
            Text("Tasks Done", color = MaterialTheme.colorScheme.onTertiary)
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
fun StatRows(stats: ListenerStats) {
    Column {
//        Text("Stats", color = Color.White, fontSize = 16.sp)
//        Spacer(Modifier.height(12.dp))
        StatCard("Answered Calls", stats.totalAnsweredCalls.toString())
        StatCard("Missed Calls", stats.totalMissedCalls.toString())
        StatCard("Ratings", stats.totalRatings.toString())
        StatCard("Reviews", stats.totalReviews.toString())
    }
}

@Composable
fun StatCard(name: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Medium)
            }
            Text(value, color = Color.Green, fontSize = 12.sp)
        }
    }
}