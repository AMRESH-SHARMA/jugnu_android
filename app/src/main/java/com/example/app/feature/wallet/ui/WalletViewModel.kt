package com.example.app.feature.wallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.user.domain.model.UserRole
import com.example.app.core.user.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    repo: UserPreferencesRepository
) : ViewModel() {

    val userPrefs: StateFlow<Pair<String, UserRole>> =
        repo.userPrefsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "" to UserRole.CUSTOMER
        )
}