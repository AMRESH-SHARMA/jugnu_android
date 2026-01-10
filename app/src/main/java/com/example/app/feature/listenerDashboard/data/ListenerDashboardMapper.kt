package com.example.app.feature.listenerDashboard.data

import com.example.app.feature.listenerDashboard.domain.ListenerStats

fun ListenerStatsDto.toDomain(): ListenerStats =
    ListenerStats(
        avatar = avatar,
        name = name,
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
