package com.example.app.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.remoteconfig.OfferConfig
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.core.session.UserSession
import com.example.app.feature.wallet.domain.usecase.GetWalletBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getBalance: GetWalletBalanceUseCase,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    // ---------------------------------------------------------
    // Balance state (UNCHANGED)
    // ---------------------------------------------------------
    private val _balance = MutableStateFlow<Long>(0)
    val balance = _balance.asStateFlow()

    // ---------------------------------------------------------
    // 🔔 One-shot UI event (navigation trigger)
    // ---------------------------------------------------------
    private val _showOfferEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val showOfferEvent = _showOfferEvent.asSharedFlow()

    // ---------------------------------------------------------
    // Session guard (VERY IMPORTANT)
    // ---------------------------------------------------------
    private var offerCheckedThisSession = false

    init {
        refreshBalance()
        checkAndEmitOffer()
    }

    // ---------------------------------------------------------
    // Core offer decision logic
    // ---------------------------------------------------------
    private fun checkAndEmitOffer() {
        // 🔐 Prevent multiple checks in same session
        if (offerCheckedThisSession) return
        offerCheckedThisSession = true

        viewModelScope.launch {
            val offer = RemoteConfig.getOffer() ?: return@launch
            Log.d("RTM", "OFFER ${offer.title}, ${offer.body}")
            if (!offer.enabled) return@launch

            val today = LocalDate.now().toString()
            val signature = offer.signature()

            val lastDate = userPrefs.getLastOfferShownDate()
            val lastSignature = userPrefs.getLastOfferSignature()

            // ✅ Show if:
            // - offer content changed OR
            // - not shown today
            val shouldShow =
                lastSignature != signature || lastDate != today

            if (shouldShow) {
                _showOfferEvent.emit(Unit)
            }
        }
    }

    // ---------------------------------------------------------
    // Call when dialog is dismissed
    // ---------------------------------------------------------
    fun markOfferShown() {
        val offer = RemoteConfig.getOffer() ?: return
        val today = LocalDate.now().toString()

        viewModelScope.launch {
            userPrefs.markOfferShown(
                signature = offer.signature(),
                date = today
            )
        }
    }

    fun OfferConfig.signature(): String {
        return "${title.trim()}|${body.trim()}".hashCode().toString()
    }

    // ---------------------------------------------------------
    // Balance refresh (UNCHANGED)
    // ---------------------------------------------------------
    private fun refreshBalance() {
        viewModelScope.launch {
            when (val res = getBalance()) {
                is ApiResult.Success -> _balance.value = res.data.balanceCoins
                is ApiResult.Error -> { /* ignore */
                }
            }
        }
    }
}
