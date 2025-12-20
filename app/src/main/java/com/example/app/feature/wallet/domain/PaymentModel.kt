package com.example.app.feature.wallet.domain

import java.time.Instant

data class WalletModel(
    val balance: Long
)

data class WalletHistoryModel(
    val amount: Long,
    val type: WalletTxnType,
    val reason: String,
    val balanceAfter: Long,
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
