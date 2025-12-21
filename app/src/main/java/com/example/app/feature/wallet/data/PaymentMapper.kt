package com.example.app.feature.wallet.data

import android.annotation.SuppressLint
import com.example.app.feature.wallet.domain.WalletHistoryModel
import com.example.app.feature.wallet.domain.WalletModel
import com.example.app.feature.wallet.domain.WalletTxnType
import java.time.Instant

fun WalletDto.toDomain(): WalletModel {
    return WalletModel(balance = balance)
}

@SuppressLint("NewApi")
fun WalletHistoryDto.toDomain(): WalletHistoryModel {
    return WalletHistoryModel(
        amount = amount,
        type = WalletTxnType.valueOf(type),
        reason = reason,
        balanceAfter = balanceAfter,
        time = Instant.parse(createdAt) // ISO 8601
    )
}
