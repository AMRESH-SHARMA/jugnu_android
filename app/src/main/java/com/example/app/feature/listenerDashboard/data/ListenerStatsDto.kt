package com.example.app.feature.listenerDashboard.data

data class ListenerStatsDto(
    val avatar: String,
    val name: String,
    val isAvailable: Boolean,
    val uniqueCallers: Long,
    val totalAnsweredCalls: Long,
    val totalMissedCalls: Long,
    val totalTalkSeconds: Long,
    val grossEarnings: Long,
    val platformPercent: Long,
    val platformFeeTotal: Long,
    val netEarnings: Long,
    val totalRatings: Long,
    val totalReviews: Long
)

data class RevenueTrendDto(
    val dailyRevenue: List<DailyRevenueDto>?
)

data class DailyRevenueDto(
    val date: String,
    val grossEarnings: Long,
    val netEarnings: Long
)
