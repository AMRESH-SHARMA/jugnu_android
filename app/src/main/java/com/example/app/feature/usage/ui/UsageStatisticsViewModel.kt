package com.example.app.feature.usage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.feature.usage.data.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsageStatisticsViewModel @Inject constructor(
    private val repository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsageStatisticsUiState())
    val uiState: StateFlow<UsageStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectFilter(filter: FilterType) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            customFromDate = if (filter != FilterType.CUSTOM) null else _uiState.value.customFromDate,
            customToDate = if (filter != FilterType.CUSTOM) null else _uiState.value.customToDate
        )
        loadData()
    }

    fun setCustomFromDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(customFromDate = date)
        if (_uiState.value.customToDate != null) {
            loadData()
        }
    }

    fun setCustomToDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(customToDate = date)
        if (_uiState.value.customFromDate != null) {
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val (fromDate, toDate) = getDateRange()

            when (val result = repository.getUsageStatistics(fromDate, toDate)) {
                is ApiResult.Success -> {
                    val data = result.data
                    val totalAudio = data.sumOf { it.audioMinutes }
                    val totalVideo = data.sumOf { it.videoMinutes }

                    _uiState.value = _uiState.value.copy(
                        chartData = data,
                        totalAudioMinutes = totalAudio,
                        totalVideoMinutes = totalVideo,
                        isLoading = false,
                        error = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load usage statistics"
                    )
                }
            }
        }
    }

    private fun getDateRange(): Pair<LocalDate, LocalDate> {
        val currentState = _uiState.value
        return when (currentState.selectedFilter) {
            FilterType.TEN_DAYS -> {
                val toDate = LocalDate.now()
                val fromDate = toDate.minusDays(9)
                fromDate to toDate
            }
            FilterType.THIRTY_DAYS -> {
                val toDate = LocalDate.now()
                val fromDate = toDate.minusDays(29)
                fromDate to toDate
            }
            FilterType.CUSTOM -> {
                val fromDate = currentState.customFromDate ?: LocalDate.now().minusDays(9)
                val toDate = currentState.customToDate ?: LocalDate.now()
                fromDate to toDate
            }
        }
    }

    fun retry() {
        loadData()
    }
}

data class UsageStatisticsUiState(
    val selectedFilter: FilterType = FilterType.TEN_DAYS,
    val chartData: List<DailyUsage> = emptyList(),
    val totalAudioMinutes: Int = 0,
    val totalVideoMinutes: Int = 0,
    val customFromDate: LocalDate? = null,
    val customToDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DailyUsage(
    val date: LocalDate,
    val audioMinutes: Int,
    val videoMinutes: Int
)

enum class FilterType {
    TEN_DAYS,
    THIRTY_DAYS,
    CUSTOM
}
