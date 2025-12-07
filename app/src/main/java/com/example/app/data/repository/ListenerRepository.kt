package com.example.app.data.repository

import com.example.app.data.mapper.toDomain
import com.example.app.data.remote.api.ListenerApi
import com.example.app.domain.model.Listener
import javax.inject.Inject

class ListenerRepository @Inject constructor(
    private val api: ListenerApi
) {
    suspend fun getListeners(): List<Listener> {
        return try {
            val res = api.getListeners()
            res.data.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
