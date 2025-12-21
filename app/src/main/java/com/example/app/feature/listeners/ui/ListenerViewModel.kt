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

    val listeners = MutableStateFlow<List<ListenerModel>>(emptyList())

    val presenceMap: StateFlow<Map<String, PresenceState>> =
        remotePresenceStore.states

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        when (val result = getListeners()) {

            is ApiResult.Success -> {
                listeners.value = result.data
            }

            is ApiResult.Error -> {
                listeners.value = emptyList()
                UiEventBus.emit(
                    UiEvent.ShowSnackbar(result.message ?: "Unable to load listeners")
                )
            }
        }
    }
}
