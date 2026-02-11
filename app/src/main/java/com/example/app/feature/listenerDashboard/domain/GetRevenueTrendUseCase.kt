package com.example.app.feature.listenerDashboard.domain

import com.example.app.core.network.ApiResult
import com.example.app.feature.listenerDashboard.data.ListenerDashboardRepository
import javax.inject.Inject

class GetRevenueTrendUseCase @Inject constructor(
    private val repository: ListenerDashboardRepository
) {
    suspend operator fun invoke(listenerId: Long, days: Int): ApiResult<RevenueTrend> {
        return repository.getRevenueTrend(listenerId, days)
    }
}
