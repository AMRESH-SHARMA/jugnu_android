package com.example.app.feature.listenerDashboard.domain

data class ListenerStats(
    val avatar: String,
    val name: String,
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
