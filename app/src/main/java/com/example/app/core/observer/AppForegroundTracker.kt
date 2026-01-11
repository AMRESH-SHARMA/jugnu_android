package com.example.app.core.observer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppForegroundTracker @Inject constructor() : DefaultLifecycleObserver {

    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        _isForeground.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        _isForeground.value = false
    }
}
