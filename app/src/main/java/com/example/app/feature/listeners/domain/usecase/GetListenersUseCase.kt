package com.example.app.feature.listeners.domain.usecase


import com.example.app.core.network.ApiResult
import com.example.app.feature.listeners.data.ListenerRepository
import com.example.app.feature.listeners.domain.ListenerModel
import javax.inject.Inject

class GetListenersUseCase @Inject constructor(
    private val repo: ListenerRepository
) {
    suspend operator fun invoke(): ApiResult<List<ListenerModel>> {
        return repo.getListeners()
    }
}



