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

/*
class PaymentRepository @Inject constructor(
    private val api: PaymentApi
) {

    suspend fun getBalance(userId: Long): ApiResult<WalletModel> {
        return try {
            val res = api.getBalance(userId)

            if (res.success) {
                ApiResult.Success(res.data.toDomain())
            } else {
                ApiResult.Error(res.message)
            }

        } catch (e: Exception) {
            ApiResult.Error(
                message = e.localizedMessage,
                exception = e
            )
        }
    }

    suspend fun getWalletHistory(
        userId: Long,
        page: Int,
        size: Int
    ): ApiResult<WalletHistoryPage> {

        return try {
            val res = api.getWalletHistory(userId, page, size)

            if (res.success) {
                ApiResult.Success(
                    WalletHistoryPage(
                        items = res.data.items.map { it.toDomain() },
                        total = res.data.total,
                        page = res.data.page,
                        size = res.data.size
                    )
                )
            } else {
                ApiResult.Error(res.message)
            }

        } catch (e: Exception) {
            ApiResult.Error(
                message = e.localizedMessage,
                exception = e
            )
        }
    }
    suspend fun withdraw(
        userId: Long,
        amount: Long
    ): ApiResult<CreateWithdrawResponseDto> {
        return try {
            val res = api.withdraw(
                CreateWithdrawRequestDto(
                    amount = amount,
                    reason = "Withdraw"
                )
            )

            if (res.success) {
                ApiResult.Success(CreateWithdrawResponseDto(
                    withdrawId = res.data.withdrawId,
                    status = res.data.status
                ))
            } else {
                ApiResult.Error(res.message)
            }

        } catch (e: Exception) {
            ApiResult.Error(
                message = e.localizedMessage,
                exception = e
            )
        }
    }
    suspend fun addMoney(
        userId: Long,
        amount: Long,
        currency: String,
        description: String? = null

    ): ApiResult<CreateOrderResponseDto> {
        return try {
            val res = api.createOrder(
                CreateOrderRequestDto(
                    amount = amount,
                    currency = currency,
                    description = description
                )
            )

            if (res.success) {
                ApiResult.Success(
                    CreateOrderResponseDto(
                        orderId = res.data.orderId,
                        status = res.data.status,
                    )
                )
            } else {
                ApiResult.Error(res.message)
            }
        } catch (e: Exception){
            ApiResult.Error(
                message = e.localizedMessage,
                exception = e
            )
        }
    }


}

 */
