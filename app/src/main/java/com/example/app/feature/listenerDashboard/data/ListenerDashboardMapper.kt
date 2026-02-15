package com.example.app.feature.listenerDashboard.data

import com.example.app.feature.listenerDashboard.domain.DailyRevenue
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import com.example.app.feature.listenerDashboard.domain.RevenueTrend
import java.time.LocalDate

fun ListenerStatsDto.toDomain(): ListenerStats =
    ListenerStats(
        avatar = avatar,
        name = name,
        isAvailable = isAvailable,
        uniqueCallers = uniqueCallers,
        totalAnsweredCalls = totalAnsweredCalls,
        totalMissedCalls = totalMissedCalls,
        totalTalkSeconds = totalTalkSeconds,
        grossEarnings = grossEarnings,
        platformPercent = platformPercent,
        platformFeeTotal = platformFeeTotal,
        netEarnings = netEarnings,
        totalRatings = totalRatings,
        totalReviews = totalReviews,
    )

fun RevenueTrendDto.toDomain(): RevenueTrend =
    RevenueTrend(
        dailyRevenue = dailyRevenue?.map { it.toDomain() } ?: emptyList()
    )

fun DailyRevenueDto.toDomain(): DailyRevenue =
    DailyRevenue(
        date = LocalDate.parse(date.substringBefore('T')),  // Extract date part only
        grossEarnings = grossEarnings,
        netEarnings = netEarnings
    )
