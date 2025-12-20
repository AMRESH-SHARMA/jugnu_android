package com.example.app.feature.wallet.domain.usecase

import com.example.app.core.network.ApiResult
import com.example.app.feature.wallet.data.PaymentRepository
import com.example.app.feature.wallet.domain.WalletHistoryPage
import javax.inject.Inject

class GetWalletHistoryUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(
        userId: Long,
        page: Int,
        size: Int
    ): ApiResult<WalletHistoryPage> {
        return repo.getWalletHistory(userId, page, size)
    }
}