package com.example.app.feature.wallet.domain.usecase

import com.example.app.feature.wallet.data.PaymentRepository
import javax.inject.Inject

class WithdrawUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
//    suspend operator fun invoke(
//        amount: Long,
//        method: String
//    ): ApiResult<WithdrawResultModel> {
//        return repo.withdraw(amount, method)
//    }
}
