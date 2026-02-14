package com.example.app.feature.listenerDashboard.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ListenerDashBoardApi {
    @GET("listeners/stats")
    suspend fun getListenerStats(
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): BaseResponse<ListenerStatsDto>

    @GET("listeners/{id}/revenue-trend")
    suspend fun getRevenueTrend(
        @Path("id") listenerId: Long,
        @Query("days") days: Int
    ): BaseResponse<RevenueTrendDto>
}


