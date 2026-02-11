package com.example.app.feature.wallet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.session.UserSession
import com.example.app.feature.wallet.domain.AmountFlowType
import com.example.app.feature.wallet.domain.usecase.AddMoneyUseCase
import com.example.app.feature.wallet.domain.usecase.GetWalletBalanceUseCase
import com.example.app.feature.wallet.domain.usecase.WithdrawMoneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterAmountViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getBalanceUseCase: GetWalletBalanceUseCase,
    private val addMoneyUseCase: AddMoneyUseCase,
    private val withdrawMoneyUseCase: WithdrawMoneyUseCase
) : ViewModel() {

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _upiId = MutableStateFlow("")
    val upiId = _upiId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    val canContinue = amount.map {
        it.toLongOrNull()?.let { value -> value > 0 } == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _currentBalance = MutableStateFlow<Long?>(null)
    val currentBalance = _currentBalance.asStateFlow()

    init {
        fetchBalance()
    }

    private fun fetchBalance() {
        viewModelScope.launch {
            when (val res = getBalanceUseCase()) {
                is ApiResult.Success ->
                    _currentBalance.value = res.data.balanceCoins

                is ApiResult.Error ->
                    _currentBalance.value = null
            }
        }
    }

    // -----------------------------
    // Input handlers
    // -----------------------------
    fun onAmountChange(input: String) {
        if (input.all { it.isDigit() }) {
            _amount.value = input
            _error.value = null
        }
    }

    fun onUpiIdChange(input: String) {
        _upiId.value = input
        _error.value = null
    }

    fun onQuickAmountClick(value: Int) {
        _amount.value = value.toString()
        _error.value = null
    }

    // -----------------------------
    // SUBMIT ACTION
    // -----------------------------

    private companion object {
        const val MIN_AMOUNT = 1L
        const val MAX_AMOUNT = 50_000L
    }

    private fun validateAmount(
        flowType: AmountFlowType,
        amount: Long,
        currentBalance: Long?
    ): String? {

        // Common validation
        when {
            amount < MIN_AMOUNT ->
                return "Minimum amount is ₹$MIN_AMOUNT"

            amount > MAX_AMOUNT ->
                return "Maximum amount is ₹$MAX_AMOUNT"
        }

        // Withdraw-specific validation
        if (flowType == AmountFlowType.WITHDRAW && currentBalance != null) {
            if (amount > currentBalance) {
                return "Insufficient balance"
            }
        }

        return null
    }

    fun onContinue(flowType: AmountFlowType) {
        val amountValue = amount.value.toLongOrNull()
        val userId = userSession.accountId

        if (userId == 0L || amountValue == null || amountValue <= 0) {
            _error.value = "Invalid amount"
            return
        }

        // Validate UPI ID for withdraw
        if (flowType == AmountFlowType.WITHDRAW && upiId.value.isBlank()) {
            _error.value = "Please enter UPI ID"
            return
        }

        val validationError = validateAmount(
            flowType = flowType,
            amount = amountValue,
            currentBalance = currentBalance.value
        )

        if (validationError != null) {
            _error.value = validationError
            return
        }

        viewModelScope.launch {
            _loading.value = true

            val result = when (flowType) {
                AmountFlowType.ADD -> {
                    addMoneyUseCase(
                        userId = userId,
                        amount = amountValue,
                        currency = "INR",
                        description = "Wallet top-up"
                    )
                }

                AmountFlowType.WITHDRAW -> {
                    withdrawMoneyUseCase(
                        userId = userId,
                        amount = amountValue,
                        destinationRef = upiId.value.trim()
                    )
                }
            }

            _loading.value = false

            when (result) {
                is ApiResult.Success -> {
                    _success.value = true
                }

                is ApiResult.Error -> {
                    _error.value = result.message ?: "Something went wrong"
                }
            }
        }
    }
}

