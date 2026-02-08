package com.example.app.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.feature.user.domain.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserUiState {
    object Idle : UserUiState()
    object LoggingOut : UserUiState()
    object LoggedOut : UserUiState()
    data class Error(val message: String) : UserUiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = UserUiState.LoggingOut
                logoutUseCase()
                _uiState.value = UserUiState.LoggedOut
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Logout failed")
            }
        }
    }
}
