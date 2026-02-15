package com.example.app.feature.listenerDashboard.ui

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/* ---------------------------------------------------
   DATA + RANGE
--------------------------------------------------- */

enum class StatsFilter { TODAY, ALL_TIME, DAYS_30, CUSTOM }

enum class RevenueTrendFilter { DAYS_7, DAYS_30, DAYS_90 }

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
    val isRefreshing by vm.isRefreshing.collectAsState()
    val isLoadingFilter by vm.isLoadingFilter.collectAsState()
    val showTimeoutMessage by vm.showTimeoutMessage.collectAsState()

    LaunchedEffect(Unit) { vm.load() }
    
    // Show timeout snackbar using global snackbar
    LaunchedEffect(showTimeoutMessage) {
        if (showTimeoutMessage) {
            com.example.app.core.ui.SnackbarManager.showError("Server Timeout - Please try again")
        }
    }

    // Get the saved tab from navigation back stack entry
    val navBackStackEntry = navController.currentBackStackEntry
    val savedTab = navBackStackEntry?.savedStateHandle?.get<String>("selected_tab")
    
    var selectedTab by remember { 
        mutableStateOf(
            savedTab?.let { 
                try { ListenerTab.valueOf(it) } catch (e: Exception) { ListenerTab.DASHBOARD }
            } ?: ListenerTab.DASHBOARD
        ) 
    }
    
    // Save tab state when it changes
    LaunchedEffect(selectedTab) {
        navBackStackEntry?.savedStateHandle?.set("selected_tab", selectedTab.name)
    }
    
    var showPicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf(DatePickerType.FROM) }
    val pickerState = rememberDateRangePickerState()
    var activeFilter by remember { mutableStateOf(StatsFilter.ALL_TIME) }
    var customFromDate by remember { mutableStateOf<LocalDate?>(null) }
    var customToDate by remember { mutableStateOf<LocalDate?>(null) }
    var revenueTrendFilter by remember { mutableStateOf(RevenueTrendFilter.DAYS_7) }
    Scaffold(
        bottomBar = {
            ListenerBottomTabBar(
                selected = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            if (selectedTab == ListenerTab.DASHBOARD && uiState is UiState.Success) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Test Snackbar Button
                    FloatingActionButton(
                        onClick = {
                            when ((0..3).random()) {
                                0 -> com.example.app.core.ui.SnackbarManager.showSuccess("Success! Operation completed")
                                1 -> com.example.app.core.ui.SnackbarManager.showError("Error! Something went wrong")
                                2 -> com.example.app.core.ui.SnackbarManager.showWarning("Warning! Please check this")
                                3 -> com.example.app.core.ui.SnackbarManager.showInfo("Info: New update available")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Snackbar",
                            tint = Color.White
                        )
                    }
                    
                    // Refresh Button
                    FloatingActionButton(
                        onClick = { vm.refresh() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val currentState = uiState) {
                            is UiState.Success -> {
                                val stats = currentState.data
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    ImprovedHeaderSection(
                                        username = stats.name,
                                        avatarUrl = stats.avatar
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    // Availability Status Card
                                    AvailabilityStatusCard(
                                        isAvailable = vm.isAvailable.collectAsState().value,
                                        isUpdating = vm.isUpdatingAvailability.collectAsState().value,
                                        onAvailabilityToggle = { vm.toggleAvailability() }
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    // Filter Section
                                    FilterSection(
                                        activeFilter = activeFilter,
                                        customFromDate = customFromDate,
                                        customToDate = customToDate,
                                        onCustomDateClick = { type ->
                                            datePickerType = type
                                            showPicker = true
                                        },
                                        onFilterChange = { filter ->
                                            if (activeFilter != filter) {
                                                activeFilter = filter
                                                when (filter) {
                                                    StatsFilter.TODAY -> {
                                                        customFromDate = null
                                                        customToDate = null
                                                        val today = LocalDate.now().toString()
                                                        vm.setDateRange(today, today)
                                                        if (vm.shouldLoadForFilter(filter, null, null)) {
                                                            vm.load()
                                                            vm.markFilterAsLoaded(filter, null, null)
                                                        }
                                                    }
                                                    StatsFilter.ALL_TIME -> {
                                                        customFromDate = null
                                                        customToDate = null
                                                        vm.setDateRange(null, null)
                                                        if (vm.shouldLoadForFilter(filter, null, null)) {
                                                            vm.load()
                                                            vm.markFilterAsLoaded(filter, null, null)
                                                        }
                                                    }
                                                    StatsFilter.DAYS_30 -> {
                                                        customFromDate = null
                                                        customToDate = null
                                                        val today = LocalDate.now()
                                                        val from = today.minusDays(30).toString()
                                                        val to = today.toString()
                                                        vm.setDateRange(from, to)
                                                        if (vm.shouldLoadForFilter(filter, null, null)) {
                                                            vm.load()
                                                            vm.markFilterAsLoaded(filter, null, null)
                                                        }
                                                    }
                                                    StatsFilter.CUSTOM -> {
                                                        // Just switch to custom, don't load until both dates are selected
                                                        if (customFromDate != null && customToDate != null) {
                                                            vm.setDateRange(customFromDate.toString(), customToDate.toString())
                                                            if (vm.shouldLoadForFilter(filter, customFromDate.toString(), customToDate.toString())) {
                                                                vm.load()
                                                                vm.markFilterAsLoaded(filter, customFromDate.toString(), customToDate.toString())
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    ImprovedOverviewCards(stats)
                                    
                                    Spacer(Modifier.height(20.dp))
                                    
                                    RevenueChartCard(
                                        stats = stats,
                                        selectedFilter = revenueTrendFilter,
                                        onFilterChange = { revenueTrendFilter = it },
                                        viewModel = vm
                                    )
                                    
                                    Spacer(Modifier.height(20.dp))

                                    ImprovedTasksCard(stats)
                                    
                                    Spacer(Modifier.height(20.dp))

                                    ImprovedStatRows(stats)
                                    
                                    Spacer(Modifier.height(80.dp))
                                }
                            }

                            is UiState.Loading -> {
                                // Show centered loading only on first load (when no previous data)
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is UiState.Error -> {
                                // Show error in snackbar and keep previous data if available
                                LaunchedEffect(currentState.message) {
                                    com.example.app.core.ui.SnackbarManager.showError(
                                        currentState.message ?: "Failed to load data"
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Failed to load data",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        TextButton(onClick = { vm.load() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Loading overlay for refresh or filter change
                        if (isRefreshing || isLoadingFilter) {
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

                ListenerTab.RECENTS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header for Recents tab
                        ListenerRecentsHeader()
                        
                        // Recents content
                        com.example.app.feature.recents.ui.RecentsScreen(
                            navController = navController
                        )
                    }
                }

                ListenerTab.SETTINGS -> {
                    com.example.app.feature.user.ui.UserSettingScreen(
                        onWalletClick = {
                            navController.navigate(Routes.Graph.WALLET) {
                                launchSingleTop = true
                            }
                        },
                        onUsageClick = {
                            navController.navigate(Routes.Screen.Usage.STATISTICS) {
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

            // ---------- DATE PICKER ----------
            if (showPicker) {
                CustomDatePickerDialog(
                    onDismiss = { showPicker = false },
                    onDateSelected = { date ->
                        when (datePickerType) {
                            DatePickerType.FROM -> {
                                customFromDate = date
                                if (customToDate != null) {
                                    val fromStr = date.toString()
                                    val toStr = customToDate.toString()
                                    vm.setDateRange(fromStr, toStr)
                                    if (vm.shouldLoadForFilter(StatsFilter.CUSTOM, fromStr, toStr)) {
                                        vm.load()
                                        vm.markFilterAsLoaded(StatsFilter.CUSTOM, fromStr, toStr)
                                    }
                                }
                            }
                            DatePickerType.TO -> {
                                customToDate = date
                                if (customFromDate != null) {
                                    val fromStr = customFromDate.toString()
                                    val toStr = date.toString()
                                    vm.setDateRange(fromStr, toStr)
                                    if (vm.shouldLoadForFilter(StatsFilter.CUSTOM, fromStr, toStr)) {
                                        vm.load()
                                        vm.markFilterAsLoaded(StatsFilter.CUSTOM, fromStr, toStr)
                                    }
                                }
                            }
                        }
                        showPicker = false
                    },
                    datePickerType = datePickerType,
                    customFromDate = customFromDate,
                    customToDate = customToDate
                )
            }
        }
    }
}

/* ---------------------------------------------------
   IMPROVED HEADER
--------------------------------------------------- */
@Composable
fun ImprovedHeaderSection(username: String, avatarUrl: String) {
    val currentHour = LocalTime.now().hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box {
                // Gradient ring
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            shape = CircleShape
                        )
                )
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/* ---------------------------------------------------
   AVAILABILITY STATUS CARD
--------------------------------------------------- */
@Composable
fun AvailabilityStatusCard(
    isAvailable: Boolean,
    isUpdating: Boolean,
    onAvailabilityToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isAvailable)
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                else
                    Color(0xFFFFC107).copy(alpha = 0.2f)
            )
            .clickable(enabled = !isUpdating) { onAvailabilityToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Icon(
                imageVector = if (isAvailable) Icons.Default.Call else androidx.compose.material.icons.Icons.Default.CallMissed,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFFFC107)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAvailable) "Available" else "Silent Mode",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isAvailable) 
                        Color(0xFF4CAF50)
                    else 
                        Color(0xFFFFC107)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isAvailable) "Receiving calls" else "Not receiving calls",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Tap hint
            Text(
                text = "Tap to change",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }
    }
}

/* ---------------------------------------------------
   FILTER SECTION
--------------------------------------------------- */
@Composable
fun FilterSection(
    activeFilter: StatsFilter,
    onFilterChange: (StatsFilter) -> Unit,
    customFromDate: LocalDate?,
    customToDate: LocalDate?,
    onCustomDateClick: (DatePickerType) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = activeFilter == StatsFilter.TODAY,
                onClick = { onFilterChange(StatsFilter.TODAY) },
                label = { Text("Today") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
            
            FilterChip(
                selected = activeFilter == StatsFilter.ALL_TIME,
                onClick = { onFilterChange(StatsFilter.ALL_TIME) },
                label = { Text("All Time") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = activeFilter == StatsFilter.DAYS_30,
                onClick = { onFilterChange(StatsFilter.DAYS_30) },
                label = { Text("Last 30 Days") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = activeFilter == StatsFilter.CUSTOM,
                onClick = { onFilterChange(StatsFilter.CUSTOM) },
                label = { Text("Custom") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }

        if (activeFilter == StatsFilter.CUSTOM) {
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

enum class DatePickerType {
    FROM, TO
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
        customFromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } else null
    
    val maxDateMillis = if (datePickerType == DatePickerType.FROM && customToDate != null) {
        customToDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } else null
    
    val datePickerState = rememberDatePickerState(
        selectableDates = object : androidx.compose.material3.SelectableDates {
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
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
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

/* ---------------------------------------------------
   REVENUE CHART CARD
--------------------------------------------------- */
@Composable
fun RevenueChartCard(
    stats: ListenerStats,
    selectedFilter: RevenueTrendFilter,
    onFilterChange: (RevenueTrendFilter) -> Unit,
    viewModel: ListenerDashboardViewModel
) {
    val revenueTrend by viewModel.revenueTrend.collectAsState()
    
    // Load revenue trend when filter changes
    LaunchedEffect(selectedFilter) {
        val days = when (selectedFilter) {
            RevenueTrendFilter.DAYS_7 -> 7
            RevenueTrendFilter.DAYS_30 -> 30
            RevenueTrendFilter.DAYS_90 -> 90
        }
        viewModel.loadRevenueTrend(days, selectedFilter)
    }
    
    val (revenueData, periodLabel) = remember(revenueTrend, selectedFilter) {
        val label = when (selectedFilter) {
            RevenueTrendFilter.DAYS_7 -> "Last 7 days"
            RevenueTrendFilter.DAYS_30 -> "Last 30 days"
            RevenueTrendFilter.DAYS_90 -> "Last 90 days"
        }
        
        val data = try {
            revenueTrend?.dailyRevenue?.map { 
                Pair(it.date, it.netEarnings.toFloat())
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        Pair(data, label)
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "chart_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revenue Trend",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "₹${stats.netEarnings}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // Segmented Button Filter
                SegmentedButton(
                    selectedFilter = selectedFilter,
                    onFilterChange = onFilterChange
                )
            }

            Spacer(Modifier.height(16.dp))

            ImprovedRevenueLineChart(
                data = revenueData,
                animationProgress = animatedProgress,
                selectedFilter = selectedFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

@Composable
fun SegmentedButton(
    selectedFilter: RevenueTrendFilter,
    onFilterChange: (RevenueTrendFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegmentedButtonItem(
            text = "7D",
            selected = selectedFilter == RevenueTrendFilter.DAYS_7,
            onClick = { onFilterChange(RevenueTrendFilter.DAYS_7) }
        )
        SegmentedButtonItem(
            text = "30D",
            selected = selectedFilter == RevenueTrendFilter.DAYS_30,
            onClick = { onFilterChange(RevenueTrendFilter.DAYS_30) }
        )
        SegmentedButtonItem(
            text = "90D",
            selected = selectedFilter == RevenueTrendFilter.DAYS_90,
            onClick = { onFilterChange(RevenueTrendFilter.DAYS_90) }
        )
    }
}

@Composable
fun SegmentedButtonItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected)
                Color.White
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ImprovedRevenueLineChart(
    data: List<Pair<LocalDate, Float>>,
    animationProgress: Float,
    selectedFilter: RevenueTrendFilter,
    modifier: Modifier = Modifier
) {
    // Handle empty data case
    if (data.isEmpty()) {
        Box(
            modifier = modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No revenue data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    val chartColor = Color(0xFF4CAF50)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height
        val padding = 50f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw horizontal grid lines and Y-axis labels
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (chartHeight * i / gridLines)
            
            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )

            // Y-axis label
            val value = (maxValue * (gridLines - i) / gridLines).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "₹$value",
                padding - 35f,
                y + 5f,
                Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { index, (_, value) ->
            Offset(
                x = padding + (index * stepX),
                y = padding + chartHeight - (value / maxValue) * chartHeight * animationProgress
            )
        }

        // Only draw if we have data points
        if (points.isNotEmpty()) {
            // Draw gradient fill
            val gradientPath = Path().apply {
                moveTo(points.first().x, padding + chartHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, padding + chartHeight)
                close()
            }

            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        chartColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )

            // Draw line
            val linePath = Path().apply {
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
                path = linePath,
                color = chartColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = chartColor,
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = point
                )
            }

            // Draw X-axis labels (dates) - show fewer labels for larger ranges
            val labelInterval = when (selectedFilter) {
                RevenueTrendFilter.DAYS_7 -> 1  // Show all dates
                RevenueTrendFilter.DAYS_30 -> 5  // Show every 5th date
                RevenueTrendFilter.DAYS_90 -> 15 // Show every 15th date
            }
            
            data.forEachIndexed { index, (date, _) ->
                if (index % labelInterval == 0 || index == data.size - 1) {
                    val x = padding + (index * stepX)
                    val dateText = date.format(DateTimeFormatter.ofPattern("dd/MM"))
                    drawContext.canvas.nativeCanvas.drawText(
                        dateText,
                        x,
                        size.height - 5f,
                        Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 24f
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------
   IMPROVED OVERVIEW CARDS - COMPACT & PREMIUM
--------------------------------------------------- */
@Composable
fun ImprovedOverviewCards(stats: ListenerStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactOverviewCard(
            title = "Callers",
            value = stats.uniqueCallers.toString(),
            icon = Icons.Default.People,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        CompactOverviewCard(
            title = "Earnings",
            value = "₹${stats.netEarnings}",
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        CompactOverviewCard(
            title = "Answered",
            value = stats.totalAnsweredCalls.toString(),
            icon = Icons.Default.Call,
            color = Color(0xFF00BCD4),
            modifier = Modifier.weight(1f)
        )
        CompactOverviewCard(
            title = "Missed",
            value = stats.totalMissedCalls.toString(),
            icon = Icons.Default.CallMissed,
            color = Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CompactOverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.3f),
                                color.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            
            Spacer(Modifier.height(2.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}


/* ---------------------------------------------------
   IMPROVED TASK CARDS
--------------------------------------------------- */
@Composable
fun ImprovedTasksCard(stats: ListenerStats) {
    val totalCalls = stats.totalAnsweredCalls + stats.totalMissedCalls
    val answerRate = if (totalCalls > 0) {
        (stats.totalAnsweredCalls.toFloat() / totalCalls.toFloat())
    } else 0f
    
    val talkTimeHours = stats.totalTalkSeconds / 3600
    val talkTimeMinutes = (stats.totalTalkSeconds % 3600) / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(16.dp))

            // Answer Rate
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Answer Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(answerRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = answerRate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        answerRate >= 0.8f -> Color(0xFF4CAF50)
                        answerRate >= 0.5f -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

//            Spacer(Modifier.height(16.dp))
//
//            // Total Talk Time
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Total Talk Time",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
//                Text(
//                    text = "${talkTimeHours}h ${talkTimeMinutes}m",
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        fontWeight = FontWeight.SemiBold
//                    ),
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
        }
    }
}

/* ---------------------------------------------------
   IMPROVED STAT ROWS
--------------------------------------------------- */
@Composable
fun ImprovedStatRows(stats: ListenerStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Detailed Statistics",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(8.dp))
        
        ImprovedStatCard(
            name = "Gross Earnings",
            value = "₹${stats.grossEarnings}",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50)
        )
        ImprovedStatCard(
            name = "Platform Fee (${stats.platformPercent}%)",
            value = "₹${stats.platformFeeTotal}",
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFFFF9800)
        )
        ImprovedStatCard(
            name = "Total Ratings",
            value = stats.totalRatings.toString(),
            icon = Icons.Default.Star,
            color = Color(0xFFFFC107)
        )
        ImprovedStatCard(
            name = "Total Reviews",
            value = stats.totalReviews.toString(),
            icon = Icons.Default.Star,
            color = Color(0xFF9C27B0)
        )
    }
}

@Composable
fun ImprovedStatCard(
    name: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.End
            )
        }
    }
}


/* ---------------------------------------------------
   RECENTS TAB HEADER
--------------------------------------------------- */
@Composable
fun ListenerRecentsHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recent Interactions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your call and message history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}
