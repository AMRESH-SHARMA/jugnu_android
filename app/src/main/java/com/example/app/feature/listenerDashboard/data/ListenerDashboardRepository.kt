package com.example.app.feature.listenerDashboard.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import com.example.app.feature.listenerDashboard.domain.RevenueTrend
import javax.inject.Inject


class ListenerDashboardRepository @Inject constructor(
    private val api: ListenerDashBoardApi
) {
    suspend fun getListenerStats(
        fromDate: String? = null,
        toDate: String? = null
    ): ApiResult<ListenerStats> =
        safeApiCall {
            val res = api.getListenerStats(fromDate, toDate)
            if (!res.success) throw Exception(res.message)
            res.data.toDomain()
        }

    suspend fun getRevenueTrend(
        days: Int
    ): ApiResult<RevenueTrend> =
        safeApiCall {
            val res = api.getRevenueTrend(days)
            if (!res.success) throw Exception(res.message)
            res.data.toDomain()
        }
}
