package com.example.app.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.feature.login.domain.usecase.RequestOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val requestOtpUseCase: RequestOtpUseCase
) : ViewModel() {

    private val _otpRequestState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val otpRequestState: StateFlow<LoginUiState> = _otpRequestState

    fun requestOtp(phone: String) {
        _otpRequestState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val result = requestOtpUseCase.invoke(phone) // suspend call
                when (result) {
                    is ApiResult.Success -> _otpRequestState.value = LoginUiState.Success
                    is ApiResult.Error -> _otpRequestState.value =
                        LoginUiState.Error(result.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _otpRequestState.value = LoginUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }
    
    fun resetState() {
        _otpRequestState.value = LoginUiState.Idle
    }
}
