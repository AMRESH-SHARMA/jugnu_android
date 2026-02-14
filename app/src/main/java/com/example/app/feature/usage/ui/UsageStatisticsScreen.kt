package com.example.app.feature.usage.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageStatisticsScreen(
    onBackClick: () -> Unit
) {
    val viewModel: UsageStatisticsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf<DatePickerType>(DatePickerType.FROM) }

    // Show error in global snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            com.example.app.core.ui.SnackbarManager.showError(error)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Usage Statistics",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Show centered loading only on first load (when no data)
            if (uiState.isLoading && uiState.chartData.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    FilterSection(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.selectFilter(it) },
                        onCustomDateClick = { type ->
                            datePickerType = type
                            showDatePicker = true
                        },
                        customFromDate = uiState.customFromDate,
                        customToDate = uiState.customToDate
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Summary Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Audio Calls",
                            value = uiState.totalAudioMinutes,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Video Calls",
                            value = uiState.totalVideoMinutes,
                            color = Color(0xFFFFC107),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chart
                    UsageChart(
                        data = uiState.chartData,
                        selectedFilter = uiState.selectedFilter
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Legend
                    ChartLegend()

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Loading overlay for subsequent loads
                if (uiState.isLoading && uiState.chartData.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                when (datePickerType) {
                    DatePickerType.FROM -> viewModel.setCustomFromDate(date)
                    DatePickerType.TO -> viewModel.setCustomToDate(date)
                }
                showDatePicker = false
            },
            datePickerType = datePickerType,
            customFromDate = uiState.customFromDate,
            customToDate = uiState.customToDate
        )
    }
}

@Composable
fun FilterSection(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    onCustomDateClick: (DatePickerType) -> Unit,
    customFromDate: LocalDate?,
    customToDate: LocalDate?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                text = "10 Days",
                selected = selectedFilter == FilterType.TEN_DAYS,
                onClick = { onFilterSelected(FilterType.TEN_DAYS) }
            )
            FilterChip(
                text = "30 Days",
                selected = selectedFilter == FilterType.THIRTY_DAYS,
                onClick = { onFilterSelected(FilterType.THIRTY_DAYS) }
            )
            FilterChip(
                text = "Custom",
                selected = selectedFilter == FilterType.CUSTOM,
                onClick = { onFilterSelected(FilterType.CUSTOM) }
            )
        }

        if (selectedFilter == FilterType.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateSelector(
                    label = "From",
                    date = customFromDate,
                    onClick = { onCustomDateClick(DatePickerType.FROM) },
                    modifier = Modifier.weight(1f)
                )
                DateSelector(
                    label = "To",
                    date = customToDate,
                    onClick = { onCustomDateClick(DatePickerType.TO) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
fun DateSelector(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        ?: "Select date",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${value} min",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

@Composable
fun UsageChart(
    data: List<DailyUsage>,
    selectedFilter: FilterType
) {
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "chart_animation"
    )

    LaunchedEffect(data) {
        animationProgress = 0f
        animationProgress = 1f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Daily Usage",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LineChart(
                    data = data,
                    animationProgress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<DailyUsage>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    val audioColor = Color(0xFF4CAF50)
    val videoColor = Color(0xFFFFC107)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Find max value for scaling
        val maxValue = data.maxOfOrNull { maxOf(it.audioMinutes, it.videoMinutes) }?.toFloat() ?: 100f
        val yScale = chartHeight / maxValue

        // Draw horizontal grid lines
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (chartHeight * i / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )

            // Y-axis labels
            val value = (maxValue * (gridLines - i) / gridLines).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$value",
                padding - 30f,
                y + 5f,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        if (data.isNotEmpty()) {
            val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

            // Draw audio line (green)
            val audioPath = Path()
            data.forEachIndexed { index, usage ->
                val x = padding + (index * xStep)
                val y = padding + chartHeight - (usage.audioMinutes * yScale * animationProgress)

                if (index == 0) {
                    audioPath.moveTo(x, y)
                } else {
                    audioPath.lineTo(x, y)
                }
            }

            drawPath(
                path = audioPath,
                color = audioColor,
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round
                )
            )

            // Draw audio points
            data.forEachIndexed { index, usage ->
                val x = padding + (index * xStep)
                val y = padding + chartHeight - (usage.audioMinutes * yScale * animationProgress)
                drawCircle(
                    color = audioColor,
                    radius = 8f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }

            // Draw video line (yellow)
            val videoPath = Path()
            data.forEachIndexed { index, usage ->
                val x = padding + (index * xStep)
                val y = padding + chartHeight - (usage.videoMinutes * yScale * animationProgress)

                if (index == 0) {
                    videoPath.moveTo(x, y)
                } else {
                    videoPath.lineTo(x, y)
                }
            }

            drawPath(
                path = videoPath,
                color = videoColor,
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round
                )
            )

            // Draw video points
            data.forEachIndexed { index, usage ->
                val x = padding + (index * xStep)
                val y = padding + chartHeight - (usage.videoMinutes * yScale * animationProgress)
                drawCircle(
                    color = videoColor,
                    radius = 8f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }

            // Draw X-axis labels (dates)
            data.forEachIndexed { index, usage ->
                if (index % ((data.size / 5).coerceAtLeast(1)) == 0 || index == data.size - 1) {
                    val x = padding + (index * xStep)
                    val dateText = usage.date.format(DateTimeFormatter.ofPattern("dd/MM"))
                    drawContext.canvas.nativeCanvas.drawText(
                        dateText,
                        x,
                        height - 10f,
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = Color(0xFF4CAF50),
            label = "Audio Calls"
        )
        Spacer(modifier = Modifier.width(24.dp))
        LegendItem(
            color = Color(0xFFFFC107),
            label = "Video Calls"
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    datePickerType: DatePickerType,
    customFromDate: LocalDate?,
    customToDate: LocalDate?
) {
    // Calculate date constraints
    val minDateMillis = if (datePickerType == DatePickerType.TO && customFromDate != null) {
        customFromDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    } else null
    
    val maxDateMillis = if (datePickerType == DatePickerType.FROM && customToDate != null) {
        customToDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    } else null
    
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (minDateMillis != null && utcTimeMillis < minDateMillis) return false
                if (maxDateMillis != null && utcTimeMillis > maxDateMillis) return false
                return true
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text(
                    text = "OK",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                selectedDayContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                selectedDayContentColor = Color.White,
                todayContentColor = Color.White,
                todayDateBorderColor = Color.Transparent
            )
        )
    }
}

enum class DatePickerType {
    FROM, TO
}
