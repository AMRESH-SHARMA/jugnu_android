package com.example.app.feature.wallet.data

import com.example.app.core.network.ApiResult
import com.example.app.core.network.safeApiCall
import com.example.app.feature.wallet.domain.WalletHistoryPage
import com.example.app.feature.wallet.domain.WalletModel
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val api: PaymentApi
) {

    suspend fun getBalance(userId: Long): ApiResult<WalletModel> =
        safeApiCall {
            val res = api.getBalance(userId)
            res.data.toDomain()
        }

    suspend fun getWalletHistory(
        userId: Long,
        page: Int,
        size: Int
    ): ApiResult<WalletHistoryPage> =
        safeApiCall {
            val res = api.getWalletHistory(userId, page, size)

            WalletHistoryPage(
                items = res.data.items.map { it.toDomain() },
                total = res.data.total,
                page = res.data.page,
                size = res.data.size
            )
        }

    suspend fun withdraw(
        userId: Long,
        amount: Long
    ): ApiResult<CreateWithdrawResponseDto> =
        safeApiCall {
            val res = api.withdraw(
                CreateWithdrawRequestDto(
                    amount = amount,
                    reason = "Withdraw"
                )
            )
            CreateWithdrawResponseDto(
                withdrawId = res.data.withdrawId,
                status = res.data.status
            )
        }

    suspend fun addMoney(
        userId: Long,
        amount: Long,
        currency: String,
        description: String? = null
    ): ApiResult<CreateOrderResponseDto> =
        safeApiCall {
            val res = api.createOrder(
                CreateOrderRequestDto(
                    amount = amount,
                    currency = currency,
                    description = description
                )
            )
            CreateOrderResponseDto(
                orderId = res.data.orderId,
                status = res.data.status
            )
        }
}