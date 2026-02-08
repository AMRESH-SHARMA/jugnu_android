package com.example.app.feature.usage.data

import com.example.app.feature.usage.ui.DailyUsage
import java.time.LocalDate

data class UsageStatisticsDto(
    val dailyUsage: List<DailyUsageDto>
)

data class DailyUsageDto(
    val date: String, // Format: "yyyy-MM-dd"
    val audioMinutes: Int,
    val videoMinutes: Int
)

fun DailyUsageDto.toDomain(): DailyUsage {
    return DailyUsage(
        date = LocalDate.parse(date),
        audioMinutes = audioMinutes,
        videoMinutes = videoMinutes
    )
}
