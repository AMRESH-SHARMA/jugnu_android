package com.example.app.core.ui

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}
