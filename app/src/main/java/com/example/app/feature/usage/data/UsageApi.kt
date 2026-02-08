package com.example.app.feature.usage.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UsageApi {
    @GET("usage/statistics")
    suspend fun getUsageStatistics(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): BaseResponse<UsageStatisticsDto>
}
