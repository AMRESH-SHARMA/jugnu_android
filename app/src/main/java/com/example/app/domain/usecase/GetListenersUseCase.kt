package com.example.app.domain.usecase

import com.example.app.data.repository.ListenerRepository
import com.example.app.domain.model.Listener
import javax.inject.Inject

class GetListenersUseCase @Inject constructor(
    private val repo: ListenerRepository
) {
    suspend operator fun invoke(): List<Listener> {
        return try {
            repo.getListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()  // <-- prevents app crash
        }
    }
}
