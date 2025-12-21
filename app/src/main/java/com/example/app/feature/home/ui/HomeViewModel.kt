package com.example.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.session.UserSession
import com.example.app.feature.wallet.domain.usecase.GetWalletBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getBalance: GetWalletBalanceUseCase
) : ViewModel() {

    private val _balance = MutableStateFlow<Long>(0)
    val balance = _balance.asStateFlow()

    init {
        refreshBalance()
    }

    fun refreshBalance() {
        val accountId = userSession.accountId
        if (accountId == 0L) return

        viewModelScope.launch {
            when (val res = getBalance(accountId)) {
                is ApiResult.Success -> _balance.value = res.data.balance
                is ApiResult.Error -> { /* log / ignore */
                }
            }
        }
    }
}
