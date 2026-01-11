package com.example.app.feature.wallet.data

data class WalletDto(
    val balanceCoins: Long
)

data class WalletHistoryDto(
    val id: String,
    val amountCoins: Long,
    val type: String, // CREDIT / DEBIT
    val reason: String,
    val referenceType: String,
    val balanceCoinsAfter: Long,
    val createdAt: String
)

data class WalletHistoryPageDto(
    val items: List<WalletHistoryDto>,
    val total: Long,
    val page: Int,
    val size: Int
)

data class CreateWithdrawRequestDto(
    val amount: Long,
    val reason: String
)

data class CreateWithdrawResponseDto(
    val withdrawId: String,
    val status: String
)

data class CreateOrderRequestDto(
    val amount: Long,
    val currency: String,
    val description: String? = null
)

data class CreateOrderResponseDto(
    val orderId: String,
    val status: String
)


//data class RechargeOptionDto(
//    val amount: Long,
//    val bonus: Long,
//    val currency: String
//)
//
//data class WalletBalanceDto(
//    val balance: Long
//)
//
//data class CreateOrderRequestDto(
//    val amount: Long,
//    val currency: String,
//    val description: String? = null
//)
//
//data class CreateOrderResponseDto(
//    val orderId: String,
//    val status: String
//)
//
//data class CreatePaymentIntentRequestDto(
//    val orderId: String,
//    val gateway: String
//)
//
//data class CreatePaymentIntentResponseDto(
//    val paymentId: String,
//    val orderId: String,
//    val gateway: String,
//    val gatewayOrderId: String,
//    val amount: Long,
//    val status: String
//)
//
//data class CreateWithdrawRequestDto(
//    val amount: Long,
//    val reason: String
//)
//
//data class CreateWithdrawResponseDto(
//    val withdrawId: String,
//    val amount: Long,
//    val status: String
//)
//
//data class WalletHistoryDto(
//    val id: String,
//    val userId: Long,
//    val amount: Long,
//    val type: String,           // CREDIT / DEBIT
//    val reason: String,
//    val referenceType: String,  // PAYMENT / WITHDRAW
//    val referenceId: String,
//    val balanceAfter: Long,
//    val createdAt: String       // ISO string from backend
//)

//data class WalletHistoryUi(
//    val amount: String,
//    val type: WalletTxnType,
//    val reason: String,
//    val balanceAfter: String,
//    val date: String
//)