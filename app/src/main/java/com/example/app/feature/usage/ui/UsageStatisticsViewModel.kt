package com.example.app.feature.usage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsageStatisticsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UsageStatisticsUiState())
    val uiState: StateFlow<UsageStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStaticData()
    }

    fun selectFilter(filter: FilterType) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            customFromDate = if (filter != FilterType.CUSTOM) null else _uiState.value.customFromDate,
            customToDate = if (filter != FilterType.CUSTOM) null else _uiState.value.customToDate
        )
        loadStaticData()
    }

    fun setCustomFromDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(customFromDate = date)
        if (_uiState.value.customToDate != null) {
            loadStaticData()
        }
    }

    fun setCustomToDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(customToDate = date)
        if (_uiState.value.customFromDate != null) {
            loadStaticData()
        }
    }

    private fun loadStaticData() {
        viewModelScope.launch {
            val data = generateStaticData()
            val totalAudio = data.sumOf { it.audioMinutes }
            val totalVideo = data.sumOf { it.videoMinutes }

            _uiState.value = _uiState.value.copy(
                chartData = data,
                totalAudioMinutes = totalAudio,
                totalVideoMinutes = totalVideo
            )
        }
    }

    private fun generateStaticData(): List<DailyUsage> {
        val currentState = _uiState.value
        val days = when (currentState.selectedFilter) {
            FilterType.TEN_DAYS -> 10
            FilterType.THIRTY_DAYS -> 30
            FilterType.CUSTOM -> {
                if (currentState.customFromDate != null && currentState.customToDate != null) {
                    java.time.temporal.ChronoUnit.DAYS.between(
                        currentState.customFromDate,
                        currentState.customToDate
                    ).toInt() + 1
                } else {
                    10
                }
            }
        }

        val startDate = when (currentState.selectedFilter) {
            FilterType.CUSTOM -> currentState.customFromDate ?: LocalDate.now().minusDays(9)
            else -> LocalDate.now().minusDays(days.toLong() - 1)
        }

        return (0 until days).map { index ->
            val date = startDate.plusDays(index.toLong())
            DailyUsage(
                date = date,
                audioMinutes = (10..120).random(),
                videoMinutes = (5..100).random()
            )
        }
    }
}

data class UsageStatisticsUiState(
    val selectedFilter: FilterType = FilterType.TEN_DAYS,
    val chartData: List<DailyUsage> = emptyList(),
    val totalAudioMinutes: Int = 0,
    val totalVideoMinutes: Int = 0,
    val customFromDate: LocalDate? = null,
    val customToDate: LocalDate? = null
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
