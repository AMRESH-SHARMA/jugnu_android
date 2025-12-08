package com.example.app.feature.call.domain.usecase

import com.example.app.feature.call.data.CallRepository
import javax.inject.Inject

class AcceptCall @Inject constructor(
    private val repo: CallRepository
) {
    suspend operator fun invoke(callId: String) =
        repo.acceptCall(callId)
}
