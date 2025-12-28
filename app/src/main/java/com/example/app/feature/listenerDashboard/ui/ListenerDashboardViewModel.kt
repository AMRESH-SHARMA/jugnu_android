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
) : ViewModel() {

    val stats = MutableStateFlow<UiState<ListenerStats>>(UiState.Loading)

    fun load() = viewModelScope.launch {
        stats.value = UiState.Loading

        val listenerId = SessionManager.userId

        stats.value = when (val res = getListenerStats(listenerId)) {
            is ApiResult.Success -> UiState.Success(res.data)
            is ApiResult.Error -> UiState.Error(res.message)
        }
    }
}

