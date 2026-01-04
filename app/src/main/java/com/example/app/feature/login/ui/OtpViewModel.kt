package com.example.app.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.feature.login.domain.usecase.RequestOtpUseCase
import com.example.app.feature.login.domain.usecase.VerifyOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    data class Success(val data: Any? = null) : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _otpRequestState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpRequestState: StateFlow<OtpUiState> = _otpRequestState

    private val _otpVerifyState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpVerifyState: StateFlow<OtpUiState> = _otpVerifyState

    private val _resendTimer = MutableStateFlow(60) // seconds
    val resendTimer: StateFlow<Int> = _resendTimer

    // ------------------ OTP Request ------------------
    fun requestOtp(phone: String) {
        viewModelScope.launch {
            _otpRequestState.value = OtpUiState.Loading
            when(val result = requestOtpUseCase(phone)) {
                is ApiResult.Success -> _otpRequestState.value = OtpUiState.Success(result.data)
                is ApiResult.Error -> _otpRequestState.value =
                    OtpUiState.Error(result.message ?: "Failed to send OTP")
            }
        }
    }

    // ------------------ OTP Verify ------------------
    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _otpVerifyState.value = OtpUiState.Loading
            when(val result = verifyOtpUseCase(phone, otp)) {
                is ApiResult.Success -> _otpVerifyState.value = OtpUiState.Success(result.data)
                is ApiResult.Error -> _otpVerifyState.value =
                    OtpUiState.Error(result.message ?: "Invalid OTP")
            }
        }
    }

    // ------------------ Resend OTP Timer ------------------
    fun startResendTimer(totalTime: Int = 60) {
        viewModelScope.launch {
            for (i in totalTime downTo 0) {
                _resendTimer.value = i
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun resendOtp(phone: String) {
        requestOtp(phone)
        startResendTimer()
    }

    fun clearOtpError() {
        if (_otpVerifyState.value is OtpUiState.Error) {
            _otpVerifyState.value = OtpUiState.Idle
        }
    }
}
