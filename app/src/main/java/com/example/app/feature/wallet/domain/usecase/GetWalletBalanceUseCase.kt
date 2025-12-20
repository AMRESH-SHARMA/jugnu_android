package com.example.app.feature.wallet.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.wallet.data.PaymentRepository
import com.example.app.feature.wallet.domain.WalletModel
import javax.inject.Inject

class GetWalletBalanceUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(userId: Long): ApiResult<WalletModel> {
        return repo.getBalance(userId)
    }
}