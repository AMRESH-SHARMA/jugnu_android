package com.example.app.feature.listeners.domain.usecase


import androidx.paging.PagingData
import com.example.app.core.network.ApiResult
import com.example.app.feature.listeners.data.ListenerRepository
import com.example.app.feature.listeners.domain.ListenerModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListenersUseCase @Inject constructor(
    private val repo: ListenerRepository
) {

    fun invoke(): Flow<PagingData<ListenerModel>> {
        return repo.getListeners()
    }
}



