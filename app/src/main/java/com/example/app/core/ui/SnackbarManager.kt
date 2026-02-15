package com.example.app.core.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

data class SnackbarData(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val duration: Long = 3000L
)

object SnackbarManager {
    private val _snackbarState = MutableStateFlow<SnackbarData?>(null)
    val snackbarState: StateFlow<SnackbarData?> = _snackbarState

    fun showSnackbar(
        message: String,
        type: SnackbarType = SnackbarType.INFO,
        duration: Long = 3000L
    ) {
        // Log all snackbar events
        when (type) {
            SnackbarType.ERROR -> Log.e("Snackbar", "ERROR: $message")
            SnackbarType.WARNING -> Log.w("Snackbar", "WARNING: $message")
            SnackbarType.INFO -> Log.i("Snackbar", "INFO: $message")
            SnackbarType.SUCCESS -> Log.d("Snackbar", "SUCCESS: $message")
        }
        
        _snackbarState.value = SnackbarData(message, type, duration)
    }

    fun showSuccess(message: String, duration: Long = 3000L) {
        showSnackbar(message, SnackbarType.SUCCESS, duration)
    }

    fun showError(message: String, duration: Long = 3000L) {
        showSnackbar(message, SnackbarType.ERROR, duration)
    }

    fun showInfo(message: String, duration: Long = 3000L) {
        showSnackbar(message, SnackbarType.INFO, duration)
    }

    fun showWarning(message: String, duration: Long = 3000L) {
        showSnackbar(message, SnackbarType.WARNING, duration)
    }

    fun dismiss() {
        _snackbarState.value = null
    }
}
