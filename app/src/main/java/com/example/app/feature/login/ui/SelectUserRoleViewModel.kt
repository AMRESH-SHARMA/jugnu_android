package com.example.app.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.user.domain.model.UserRole
import com.example.app.core.user.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

@HiltViewModel
class SelectUserRoleViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    private val _navigateToHome = mutableStateOf(false)
    val navigateToHome: State<Boolean> get() = _navigateToHome

    fun save(accountId: String, role: UserRole) {
        viewModelScope.launch {
            repository.saveUserPrefs(accountId, role)
            _navigateToHome.value = true  // signal that save is complete
        }
    }

    fun resetNavigationFlag() {
        _navigateToHome.value = false
    }
}