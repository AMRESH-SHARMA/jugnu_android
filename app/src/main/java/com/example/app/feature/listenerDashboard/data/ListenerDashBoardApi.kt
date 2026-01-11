package com.example.app.feature.listenerDashboard.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ListenerDashBoardApi {
    @GET("listeners/{id}/stats")
    suspend fun getListenerStats(
        @Path("id") listenerId: Long,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): BaseResponse<ListenerStatsDto>
}


