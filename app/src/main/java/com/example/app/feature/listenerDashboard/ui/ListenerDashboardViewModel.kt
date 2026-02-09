package com.example.app.feature.listenerDashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.session.SessionManager
import com.example.app.core.ui.UiState
import com.example.app.feature.listenerDashboard.domain.GetListenerStatsUseCase
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerDashboardViewModel @Inject constructor(
    private val getListenerStats: GetListenerStatsUseCase,
    private val userRepository: com.example.app.feature.user.data.UserRepository
) : ViewModel() {
    val stats = MutableStateFlow<UiState<ListenerStats>>(UiState.Loading)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
    
    private val _showTimeoutMessage = MutableStateFlow(false)
    val showTimeoutMessage = _showTimeoutMessage

    private val _isAvailable = MutableStateFlow(true)
    val isAvailable = _isAvailable

    private val _isUpdatingAvailability = MutableStateFlow(false)
    val isUpdatingAvailability = _isUpdatingAvailability

    var fromDate: String? = null
    var toDate: String? = null

    fun setDateRange(from: String?, to: String?) {
        fromDate = from
        toDate = to
    }

    fun load() = viewModelScope.launch {
        val listenerId = SessionManager.userAccountId

        when (val res = getListenerStats(listenerId, fromDate, toDate)) {
            is ApiResult.Success -> stats.value = UiState.Success(res.data)
            is ApiResult.Error -> stats.value = UiState.Error(res.message)
        }
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        _showTimeoutMessage.value = false
        val listenerId = SessionManager.userAccountId
        
        val startTime = System.currentTimeMillis()
        val minDuration = 1000L // Minimum 1 second animation
        val timeout = 10000L // 10 seconds timeout

        try {
            // Launch API call with timeout
            val result = kotlinx.coroutines.withTimeoutOrNull(timeout) {
                getListenerStats(listenerId, fromDate, toDate)
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
                com.example.app.core.ui.SnackbarManager.showError(
                    result.message ?: "Failed to update availability"
                )
            }
        }
        
        _isUpdatingAvailability.value = false
    }
}
