package com.example.app.feature.listeners.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.network.ApiResult
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.domain.usecase.GetListenersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenerViewModel @Inject constructor(
    private val getListeners: GetListenersUseCase
) : ViewModel() {

    val listeners = MutableStateFlow<List<ListenerModel>>(emptyList())
    val error = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)

    init {
        load()
    }

    private fun load() = viewModelScope.launch {

        loading.value = true

        when (val result = getListeners()) {

            is ApiResult.Success -> {
                listeners.value = result.data
                error.value = null
            }

            is ApiResult.Error -> {
                listeners.value = emptyList()
                error.value = result.message ?: "Unknown error"
            }
        }

        loading.value = false
    }
}

