package com.example.app.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.feature.user.domain.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    private val logoutUseCase: LogoutUseCase,
    private val userPreferencesRepository: com.example.app.core.preferences.user.data.UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    val interestedIn = userPreferencesRepository.interestedInFlow
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = "MALE"
        )

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

    fun updateInterestedIn(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveInterestedIn(value)
        }
    }
}
