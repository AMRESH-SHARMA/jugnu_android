package com.example.app.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.feature.login.domain.SetupProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileSetupUiState {
    object Idle : ProfileSetupUiState()
    object Loading : ProfileSetupUiState()
    data class Success(val message: String) : ProfileSetupUiState()
    data class Error(val message: String) : ProfileSetupUiState()
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val setupProfileUseCase: SetupProfileUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _setupState = MutableStateFlow<ProfileSetupUiState>(ProfileSetupUiState.Idle)
    val setupState: StateFlow<ProfileSetupUiState> = _setupState

    fun setupProfile(nickname: String, gender: String, interestedIn: String) {
        viewModelScope.launch {
            _setupState.value = ProfileSetupUiState.Loading

            when (val result = setupProfileUseCase(nickname, gender, interestedIn)) {
                is ApiResult.Success -> {
                    // Save interested in preference locally
                    userPreferencesRepository.saveInterestedIn(interestedIn)
                    _setupState.value = ProfileSetupUiState.Success("Profile setup complete")
                }
                is ApiResult.Error -> {
                    _setupState.value = ProfileSetupUiState.Error(
                        result.message ?: "Failed to setup profile"
                    )
                }
            }
        }
    }
}
