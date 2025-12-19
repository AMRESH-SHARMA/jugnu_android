package com.example.app.core.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object UiEventBus {
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events

    suspend fun emit(event: UiEvent) {
        _events.emit(event)
    }
}
