package com.example.app.feature.user.domain

import com.example.app.core.network.ApiResult
import com.example.app.feature.user.data.CustomerProfileDto
import com.example.app.feature.user.data.UserRepository
import javax.inject.Inject

class GetCustomerProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<CustomerProfileDto> {
        return repository.getCustomerProfile()
    }
}
