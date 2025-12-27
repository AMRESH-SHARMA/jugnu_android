package com.example.app.feature.listenerDashboard.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ListenerDashBoardApi {
    @GET("listeners/{id}/stats")
    suspend fun getListenerStats(
        @Path("id") listenerId: Long
    ): BaseResponse<ListenerStatsDto>
}


