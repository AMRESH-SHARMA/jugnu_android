package com.example.app.feature.listeners.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.app.core.network.ApiResult
import com.example.app.core.ui.UiEvent
import com.example.app.core.ui.UiEventBus
import com.example.app.core.websocket.RemotePresenceStore
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.domain.usecase.GetListenersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerViewModel @Inject constructor(
    private val getListeners: GetListenersUseCase,
    private val remotePresenceStore: RemotePresenceStore
) : ViewModel() {

    /**
     * Network-only paging stream for Listener list
     * - No manual pagination logic
     * - Cached across navigation & rotation
     */
    val pagedListeners: Flow<PagingData<ListenerModel>> =
        getListeners.invoke()
            .cachedIn(viewModelScope)

    /**
     * Presence store is kept separate
     * - Updates from websocket
     * - Merged in UI layer for display
     */
    val presenceStore = remotePresenceStore

}
