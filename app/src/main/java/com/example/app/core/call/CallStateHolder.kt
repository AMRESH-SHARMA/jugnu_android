package com.example.app.core.call
//
//import com.example.app.feature.call.ui.CallLifecycleState
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow

/**
 * Global call lifecycle broadcaster.
 *
 * - Observed by AppNavGraph
 * - Updated by CallViewModel
 * - Contains NO business logic
 */
//object CallStateHolder {
//
//    private val _lifecycle =
//        MutableStateFlow<CallLifecycleState>(CallLifecycleState.Idle)
//
//    val lifecycle = _lifecycle.asStateFlow()
//
//    fun update(state: CallLifecycleState) {
//        _lifecycle.value = state
//    }
//
//    fun reset() {
//        _lifecycle.value = CallLifecycleState.Idle
//    }
//}
