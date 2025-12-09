package com.example.app.feature.call.domain.usecase

import com.example.app.feature.call.data.CallRepository
import javax.inject.Inject

class StartCall @Inject constructor(
    private val repo: CallRepository
) {
    suspend operator fun invoke(
        callerId: Long,
        calleeId: Long
    ) = repo.startCall(callerId, calleeId)
}
