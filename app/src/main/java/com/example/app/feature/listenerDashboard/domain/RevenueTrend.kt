package com.example.app.feature.listenerDashboard.domain

import java.time.LocalDate

data class RevenueTrend(
    val dailyRevenue: List<DailyRevenue>
)

data class DailyRevenue(
    val date: LocalDate,
    val grossEarnings: Long,
    val netEarnings: Long
)
