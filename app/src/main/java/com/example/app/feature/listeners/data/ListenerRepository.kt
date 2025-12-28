package com.example.app.feature.listeners.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.listeners.data.paging.ListenerPagingSource
import com.example.app.feature.listeners.domain.ListenerModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenerRepository @Inject constructor(
    private val api: ListenerApi
) {

    fun getListeners(
        pageSize: Int = 30
    ): Flow<PagingData<ListenerModel>> {

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = true,
                initialLoadSize = pageSize * 2
            ),
            pagingSourceFactory = {
                ListenerPagingSource(
                    api = api,
                    pageSize = pageSize
                )
            }
        ).flow
    }
}