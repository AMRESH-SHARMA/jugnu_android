package com.example.app.ui.listeners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.Listener
import com.example.app.domain.usecase.GetListenersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerViewModel @Inject constructor(
    private val getListeners: GetListenersUseCase
) : ViewModel() {

    val listeners = MutableStateFlow<List<Listener>>(emptyList())

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        listeners.value = getListeners()
    }
}

