package com.example.app.feature.listenerDashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.AppConstants
import com.example.app.core.network.ApiResult
import com.example.app.core.session.SessionManager
import com.example.app.core.ui.UiState
import com.example.app.feature.listenerDashboard.domain.GetListenerStatsUseCase
import com.example.app.feature.listenerDashboard.domain.GetRevenueTrendUseCase
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerDashboardViewModel @Inject constructor(
    private val getListenerStats: GetListenerStatsUseCase,
    private val getRevenueTrend: GetRevenueTrendUseCase,
    private val userRepository: com.example.app.feature.user.data.UserRepository
) : ViewModel() {
    val stats = MutableStateFlow<UiState<ListenerStats>>(UiState.Loading)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
    
    private val _isLoadingFilter = MutableStateFlow(false)
    val isLoadingFilter = _isLoadingFilter
    
    private val _revenueTrend = MutableStateFlow<com.example.app.feature.listenerDashboard.domain.RevenueTrend?>(null)
    val revenueTrend = _revenueTrend
    
    private val _showTimeoutMessage = MutableStateFlow(false)
    val showTimeoutMessage = _showTimeoutMessage

    private val _isAvailable = MutableStateFlow(true)
    val isAvailable = _isAvailable

    private val _isUpdatingAvailability = MutableStateFlow(false)
    val isUpdatingAvailability = _isUpdatingAvailability

    var fromDate: String? = null
    var toDate: String? = null
    
    // Track last loaded filter to avoid redundant API calls
    private var lastLoadedFilter: StatsFilter? = null
    private var lastLoadedCustomDates: Pair<String, String>? = null
    
    // Job tracking for cancellation
    private var loadJob: Job? = null
    private var revenueTrendJob: Job? = null
    private var lastLoadedRevenueTrendFilter: RevenueTrendFilter? = null

    fun setDateRange(from: String?, to: String?) {
        fromDate = from
        toDate = to
    }

    fun load() = viewModelScope.launch {
        // Cancel previous request to avoid race conditions
        loadJob?.cancel()
        
        loadJob = viewModelScope.launch {
            try {
                // Small delay to debounce rapid filter changes
                kotlinx.coroutines.delay(AppConstants.FILTER_DEBOUNCE_DELAY)
                
                // Show loading overlay if we have previous data
                if (stats.value is UiState.Success) {
                    _isLoadingFilter.value = true
                } else {
                    stats.value = UiState.Loading
                }

                when (val res = getListenerStats(fromDate, toDate)) {
                    is ApiResult.Success -> {
                        stats.value = UiState.Success(res.data)
                        // Initialize availability from API response
                        _isAvailable.value = res.data.isAvailable
                        _isLoadingFilter.value = false
                    }
                    is ApiResult.Error -> {
                        _isLoadingFilter.value = false
                        val errorMessage = res.message?.takeIf { it.isNotBlank() } 
                            ?: "Failed to load dashboard data"
                        com.example.app.core.ui.SnackbarManager.showError(errorMessage)
                        // Only set error state if we don't have previous data
                        if (stats.value !is UiState.Success) {
                            stats.value = UiState.Error(res.message)
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Silently handle cancellation - this is expected when switching filters
                _isLoadingFilter.value = false
            }
        }
    }
    
    fun shouldLoadForFilter(filter: StatsFilter, customFrom: String?, customTo: String?): Boolean {
        return when (filter) {
            StatsFilter.CUSTOM -> {
                // For CUSTOM, check if dates are different from last loaded
                if (customFrom == null || customTo == null) {
                    false // Don't load if dates not selected
                } else {
                    lastLoadedCustomDates != (customFrom to customTo)
                }
            }
            else -> {
                // For preset filters, check if different from last loaded
                lastLoadedFilter != filter
            }
        }
    }
    
    fun markFilterAsLoaded(filter: StatsFilter, customFrom: String?, customTo: String?) {
        lastLoadedFilter = filter
        if (filter == StatsFilter.CUSTOM && customFrom != null && customTo != null) {
            lastLoadedCustomDates = customFrom to customTo
        }
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        _showTimeoutMessage.value = false
        
        val startTime = System.currentTimeMillis()
        val minDuration = 1000L // Minimum 1 second animation
        val timeout = 10000L // 10 seconds timeout

        try {
            // Launch API call with timeout
            val result = kotlinx.coroutines.withTimeoutOrNull(timeout) {
                getListenerStats(fromDate, toDate)
            }
            
            // Calculate remaining time to meet minimum duration
            val elapsed = System.currentTimeMillis() - startTime
            val remainingTime = minDuration - elapsed
            if (remainingTime > 0) {
                kotlinx.coroutines.delay(remainingTime)
            }
            
            when (result) {
                is ApiResult.Success -> {
                    stats.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    stats.value = UiState.Error(result.message)
                }
                null -> {
                    // Timeout occurred
                    _showTimeoutMessage.value = true
                }
            }
        } catch (e: Exception) {
            stats.value = UiState.Error(e.message ?: "Unknown error")
        } finally {
            _isRefreshing.value = false
        }
    }
    
    fun clearTimeoutMessage() {
        _showTimeoutMessage.value = false
    }

    fun toggleAvailability() = viewModelScope.launch {
        _isUpdatingAvailability.value = true
        val newStatus = !_isAvailable.value
        
        when (val result = userRepository.updateAvailability(newStatus)) {
            is ApiResult.Success -> {
                _isAvailable.value = newStatus
                com.example.app.core.ui.SnackbarManager.showSuccess(
                    if (newStatus) "You are now available" else "Silent mode enabled"
                )
            }
            is ApiResult.Error -> {
                // Keep old value on error
                val errorMessage = result.message?.takeIf { it.isNotBlank() } 
                    ?: "Failed to update availability"
                com.example.app.core.ui.SnackbarManager.showError(errorMessage)
            }
        }
        
        _isUpdatingAvailability.value = false
    }

    fun loadRevenueTrend(days: Int, filter: RevenueTrendFilter) = viewModelScope.launch {
        // Skip if same filter already loaded
        if (lastLoadedRevenueTrendFilter == filter) {
            return@launch
        }
        
        // Cancel previous request to avoid race conditions
        revenueTrendJob?.cancel()
        
        revenueTrendJob = viewModelScope.launch {
            try {
                // Small delay to debounce rapid filter changes
                kotlinx.coroutines.delay(AppConstants.FILTER_DEBOUNCE_DELAY)
                
                when (val result = getRevenueTrend(days)) {
                    is ApiResult.Success -> {
                        _revenueTrend.value = result.data
                        lastLoadedRevenueTrendFilter = filter
                    }
                    is ApiResult.Error -> {
                        val errorMessage = result.message?.takeIf { it.isNotBlank() } 
                            ?: "Failed to load revenue trend"
                        com.example.app.core.ui.SnackbarManager.showError(errorMessage)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Silently handle cancellation - this is expected when switching filters
            }
        }
    }
}
