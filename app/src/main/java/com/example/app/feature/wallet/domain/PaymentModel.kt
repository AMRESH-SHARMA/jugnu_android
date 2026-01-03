package com.example.app.feature.wallet.domain

import java.time.Instant

data class WalletModel(
    val balanceCoins: Long
)

data class WalletHistoryModel(
    val amountCoins: Long,
    val type: WalletTxnType,
    val reason: String,
    val balanceCoinsAfter: Long,
    val time: Instant
)

enum class WalletTxnType {
    CREDIT, DEBIT
}

data class WalletHistoryPage(
    val items: List<WalletHistoryModel>,
    val total: Long,
    val page: Int,
    val size: Int
)

enum class AmountFlowType {
    ADD,
    WITHDRAW
}
