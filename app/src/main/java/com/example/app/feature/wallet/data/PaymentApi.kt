package com.example.app.feature.wallet.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentApi {

    /* ---------- WALLET ---------- */

    @GET("payments/balance/{userId}")
    suspend fun getBalance(
        @Path("userId") userId: Long
    ): BaseResponse<WalletDto>

    @GET("payments/history/{userId}")
    suspend fun getWalletHistory(
        @Path("userId") userId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): BaseResponse<WalletHistoryPageDto>

//    /* ---------- ADD MONEY ---------- */


    @POST("payments/order")
    suspend fun createOrder(
        @Body request: CreateOrderRequestDto
    ): BaseResponse<CreateOrderResponseDto>

    /* ---------- WITHDRAW ---------- */

    @POST("payments/withdraw")
    suspend fun withdraw(
        @Body request: CreateWithdrawRequestDto
    ): BaseResponse<CreateWithdrawResponseDto>
}