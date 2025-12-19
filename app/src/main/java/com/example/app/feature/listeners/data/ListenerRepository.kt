package com.example.app.feature.listeners.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.listeners.domain.ListenerModel
import javax.inject.Inject

class ListenerRepository @Inject constructor(
    private val api: ListenerApi
) {
    suspend fun getListeners(): ApiResult<List<ListenerModel>> = safeApiCall {
        val res = api.getListeners()
        if (!res.success) throw Exception(res.message)
        res.data.map { it.toDomain() }
    }
}