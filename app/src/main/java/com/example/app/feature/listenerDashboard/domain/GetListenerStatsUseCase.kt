package com.example.app.feature.listenerDashboard.domain

import com.example.app.core.network.ApiResult
import com.example.app.feature.listenerDashboard.data.ListenerDashboardRepository
import javax.inject.Inject

class GetListenerStatsUseCase @Inject constructor(
    private val repo: ListenerDashboardRepository
) {
    suspend operator fun invoke(
        fromDate: String? = null,
        toDate: String? = null
    ): ApiResult<ListenerStats> {
        return repo.getListenerStats(fromDate, toDate)
    }
}
