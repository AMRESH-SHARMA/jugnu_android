package com.example.app.feature.listeners.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.core.ui.UiEvent
import com.example.app.core.ui.UiEventBus
import com.example.app.core.websocket.PresenceState
import com.example.app.core.websocket.RemotePresenceStore
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.domain.usecase.GetListenersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerViewModel @Inject constructor(
    private val getListeners: GetListenersUseCase,
    private val remotePresenceStore: RemotePresenceStore
) : ViewModel() {

    private val _listeners = MutableStateFlow<List<ListenerModel>>(emptyList())
    val listeners: StateFlow<List<ListenerModel>> = _listeners

    private var currentPage = 1
    private val pageSize = 10

    private var isLoading = false
    private var hasMore = true

    val isLoadingState = MutableStateFlow(false)
    val hasMoreState = MutableStateFlow(true)
    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoadingState.value || !hasMoreState.value) return

        viewModelScope.launch {
            isLoadingState.value = true
            when (val result = getListeners(currentPage, pageSize)) {
                is ApiResult.Success -> {
                    val (newItems, total) = result.data
                    _listeners.value = _listeners.value + newItems
                    currentPage++
                    hasMoreState.value = _listeners.value.size < total
                }
                is ApiResult.Error -> {
                    UiEventBus.emit(UiEvent.ShowSnackbar(result.message ?: "Unable to load listeners"))
                }
            }
            isLoadingState.value = false
        }
    }
}
