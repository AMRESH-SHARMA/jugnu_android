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
    object UpdatingProfile : UserUiState()
    object ProfileUpdated : UserUiState()
    object ReportingAbuse : UserUiState()
    object AbuseReported : UserUiState()
    data class Error(val message: String) : UserUiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: com.example.app.feature.user.data.UserRepository,
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

    private val _userRole = MutableStateFlow<com.example.app.core.preferences.user.domain.UserRole?>(
        com.example.app.core.session.SessionManager.userRole
    )
    val userRole: StateFlow<com.example.app.core.preferences.user.domain.UserRole?> = _userRole

    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = UserUiState.LoggingOut
                logoutUseCase()
                _uiState.value = UserUiState.LoggedOut
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Logout failed")
                com.example.app.core.ui.SnackbarManager.showError(
                    e.message ?: "Logout failed"
                )
            }
        }
    }

    fun updateInterestedIn(value: String) {
        viewModelScope.launch {
            // Check if value has changed
            val currentValue = interestedIn.value
            if (currentValue == value) {
                _uiState.value = UserUiState.ProfileUpdated
                kotlinx.coroutines.delay(500)
                _uiState.value = UserUiState.Idle
                return@launch
            }
            
            try {
                _uiState.value = UserUiState.UpdatingProfile
                val result = userRepository.updateProfile(
                    nickname = "Anonymous",
                    gender = null, // Not updating gender, only interestedIn
                    interestedIn = value
                )
                
                when (result) {
                    is com.example.app.core.network.ApiResult.Success -> {
                        userPreferencesRepository.saveInterestedIn(value)
                        _uiState.value = UserUiState.ProfileUpdated
                        com.example.app.core.ui.SnackbarManager.showSuccess("Preference updated")
                        kotlinx.coroutines.delay(1000)
                        _uiState.value = UserUiState.Idle
                    }
                    is com.example.app.core.network.ApiResult.Error -> {
                        _uiState.value = UserUiState.Error(result.message ?: "Failed to update")
                        com.example.app.core.ui.SnackbarManager.showError(
                            result.message ?: "Failed to update preference"
                        )
                        kotlinx.coroutines.delay(2000)
                        _uiState.value = UserUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to update preference")
                com.example.app.core.ui.SnackbarManager.showError(
                    e.message ?: "Failed to update preference"
                )
                kotlinx.coroutines.delay(2000)
                _uiState.value = UserUiState.Idle
            }
        }
    }

    fun reportAbuse(description: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UserUiState.ReportingAbuse
                val result = userRepository.reportAbuse(description)
                
                when (result) {
                    is com.example.app.core.network.ApiResult.Success -> {
                        _uiState.value = UserUiState.AbuseReported
                        kotlinx.coroutines.delay(2000)
                        _uiState.value = UserUiState.Idle
                    }
                    is com.example.app.core.network.ApiResult.Error -> {
                        com.example.app.core.ui.SnackbarManager.showError(
                            result.message ?: "Failed to submit report"
                        )
                        _uiState.value = UserUiState.Error(result.message ?: "Failed to submit report")
                        kotlinx.coroutines.delay(2000)
                        _uiState.value = UserUiState.Idle
                    }
                }
            } catch (e: Exception) {
                com.example.app.core.ui.SnackbarManager.showError(
                    e.message ?: "Failed to submit report"
                )
                _uiState.value = UserUiState.Error(e.message ?: "Failed to submit report")
                kotlinx.coroutines.delay(2000)
                _uiState.value = UserUiState.Idle
            }
        }
    }
}
