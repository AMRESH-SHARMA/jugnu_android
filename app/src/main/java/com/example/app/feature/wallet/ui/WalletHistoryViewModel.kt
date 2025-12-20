package com.example.app.feature.wallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.session.UserSession
import com.example.app.feature.wallet.domain.WalletHistoryModel
import com.example.app.feature.wallet.domain.usecase.GetWalletHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletHistoryViewModel @Inject constructor(
    private val session: UserSession,
    private val getHistory: GetWalletHistoryUseCase
) : ViewModel() {

    private val _items = MutableStateFlow<List<WalletHistoryModel>>(emptyList())
    val items = _items.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private var page = 0
    private val size = 20
    private var hasMore = true

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (_loading.value || !hasMore) return

        val userId = session.accountId
        if (userId == 0L) return

        _loading.value = true

        viewModelScope.launch {
            when (val res = getHistory(userId, page, size)) {
                is ApiResult.Success -> {
                    _items.value += res.data.items
                    hasMore = _items.value.size < res.data.total
                    page++
                }

                is ApiResult.Error -> {
                    // TODO: error handling
                }
            }
            _loading.value = false
        }
    }
}
