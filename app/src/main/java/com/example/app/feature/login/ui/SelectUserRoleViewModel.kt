package com.example.app.feature.login.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.preferences.user.domain.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectUserRoleViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    private val _navigateToHome = mutableStateOf(false)
    val navigateToHome: State<Boolean> get() = _navigateToHome
    private val _savedRole = mutableStateOf<UserRole?>(null)
    val savedRole: State<UserRole?> get() = _savedRole

    fun save(accountId: Long, role: UserRole) {
        viewModelScope.launch {
            repository.saveUserPrefs(accountId, role)
            _savedRole.value = role
            _navigateToHome.value = true  // signal that save is complete
        }
    }

    fun resetNavigationFlag() {
        _navigateToHome.value = false
        _savedRole.value = null
    }
}