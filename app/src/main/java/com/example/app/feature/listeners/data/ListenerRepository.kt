package com.example.app.feature.listeners.data

import com.example.app.core.network.ApiResult
import com.example.app.feature.listeners.domain.ListenerModel
import javax.inject.Inject

class ListenerRepository @Inject constructor(
    private val api: ListenerApi
) {
    suspend fun getListeners(): ApiResult<List<ListenerModel>> {
        return try {
            val res = api.getListeners()

            if (res.success) {
                ApiResult.Success(res.data.map { it.toDomain() })
            } else {
                ApiResult.Error(res.message)
            }

        } catch (e: Exception) {
            ApiResult.Error(
                message = e.localizedMessage,
                exception = e
            )
        }
    }
}