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
    private val getListenerStats: GetListenerStatsUseCase
) : ViewModel() {
    val stats = MutableStateFlow<UiState<ListenerStats>>(UiState.Loading)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing

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
        val listenerId = SessionManager.userAccountId

        when (val res = getListenerStats(listenerId, fromDate, toDate)) {
            is ApiResult.Success -> stats.value = UiState.Success(res.data)
            is ApiResult.Error -> stats.value = UiState.Error(res.message)
        }
        _isRefreshing.value = false
    }
}
