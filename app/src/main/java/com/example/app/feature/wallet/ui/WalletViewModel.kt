package com.example.app.feature.wallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    repo: UserPreferencesRepository
) : ViewModel() {

    val userPrefs: StateFlow<Pair<Long, UserRole>> =
        repo.userPrefsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_STOP_TIMEOUT),
            initialValue = 0L to UserRole.CUSTOMER
        )
}