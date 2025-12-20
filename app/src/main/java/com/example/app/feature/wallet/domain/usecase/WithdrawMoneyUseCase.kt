package com.example.app.feature.wallet.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.wallet.data.CreateWithdrawResponseDto
import com.example.app.feature.wallet.data.PaymentRepository
import javax.inject.Inject

class WithdrawMoneyUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(
        userId: Long,
        amount: Long
    ): ApiResult<CreateWithdrawResponseDto> {
        return repo.withdraw(userId, amount)
    }
}