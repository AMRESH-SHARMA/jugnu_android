package com.example.app.feature.usage.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.usage.ui.DailyUsage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UsageRepository @Inject constructor(
    private val api: UsageApi
) {
    suspend fun getUsageStatistics(
        fromDate: LocalDate,
        toDate: LocalDate
    ): ApiResult<List<DailyUsage>> = safeApiCall {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val response = api.getUsageStatistics(
            fromDate = fromDate.format(formatter),
            toDate = toDate.format(formatter)
        )
        
        if (!response.success) throw Exception(response.message)
        
        response.data.dailyUsage.map { it.toDomain() }
    }
}
