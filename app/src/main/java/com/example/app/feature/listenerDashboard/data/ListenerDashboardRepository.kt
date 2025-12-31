package com.example.app.feature.listenerDashboard.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.listenerDashboard.domain.ListenerStats
import javax.inject.Inject


class ListenerDashboardRepository @Inject constructor(
    private val api: ListenerDashBoardApi
) {
    suspend fun getListenerStats(
        listenerId: Long,
        from: String? = null,
        to: String? = null
    ): ApiResult<ListenerStats> =
        safeApiCall {
            val res = api.getListenerStats(listenerId, from, to)
            if (!res.success) throw Exception(res.message)
            res.data.toDomain()
        }
}
