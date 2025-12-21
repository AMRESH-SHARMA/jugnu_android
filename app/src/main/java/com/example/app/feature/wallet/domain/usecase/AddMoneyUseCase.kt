package com.example.app.feature.wallet.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.wallet.data.CreateOrderResponseDto
import com.example.app.feature.wallet.data.PaymentRepository
import javax.inject.Inject

class AddMoneyUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(
        userId: Long,
        amount: Long,
        currency: String,
        description: String? = null
    ): ApiResult<CreateOrderResponseDto> {
        return repo.addMoney(userId, amount, currency, description)
    }
}