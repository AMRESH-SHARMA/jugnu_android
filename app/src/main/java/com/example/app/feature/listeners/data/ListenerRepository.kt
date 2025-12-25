package com.example.app.feature.listeners.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.listeners.domain.ListenerModel
import javax.inject.Inject

class ListenerRepository @Inject constructor(
    private val api: ListenerApi
) {
    suspend fun getListeners(
        page: Int,
        limit: Int
    ): ApiResult<Pair<List<ListenerModel>, Int>> = safeApiCall {

        val res = api.getListeners(page, limit)
        if (!res.success) throw Exception(res.message)

        val listeners = res.data.map { it.toDomain() }

        val total = (res.meta?.get("total") as? Number)?.toInt()
            ?: listeners.size

        listeners to total
    }
//    suspend fun getListeners(): ApiResult<List<ListenerModel>> = safeApiCall {
//        val res = api.getListeners()
//        if (!res.success) throw Exception(res.message)
//        res.data.map { it.toDomain() }
//    }
}