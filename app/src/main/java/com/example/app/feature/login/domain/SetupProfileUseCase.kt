package com.example.app.feature.login.domain

import com.example.app.core.network.ApiResult
import com.example.app.feature.user.data.UserRepository
import javax.inject.Inject

class SetupProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(nickname: String, gender: String, interestedIn: String): ApiResult<Unit> {
        return repository.updateProfile(nickname, gender, interestedIn)
    }
}
