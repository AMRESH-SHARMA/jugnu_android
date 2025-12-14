//Only CallManager writes here
//Everyone else only observes

package com.example.app.core.call

import com.example.app.feature.call.domain.CallModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CallStore {

    private val _call = MutableStateFlow<CallModel?>(null)
    val call: StateFlow<CallModel?> = _call

    fun set(call: CallModel) {
        _call.value = call
    }

    fun update(transform: (CallModel) -> CallModel) {
        _call.value = _call.value?.let(transform)
    }

    fun clear() {
        _call.value = null
    }

    fun current(): CallModel? = _call.value
}
