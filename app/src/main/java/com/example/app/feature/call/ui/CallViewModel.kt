package com.example.app.feature.call.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.call.CallManager
import com.example.app.core.call.CallStore
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallUiState
import com.example.app.feature.call.domain.usecase.AcceptCall
import com.example.app.feature.call.domain.usecase.EndCall
import com.example.app.feature.call.domain.usecase.RejectCall
import com.example.app.feature.call.domain.usecase.StartCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val startCallUseCase: StartCall,
    private val acceptCall: AcceptCall,
    private val rejectCallUseCase: RejectCall,
    private val endCallUseCase: EndCall,
    private val callManager: CallManager   // or CallActionCoordinator
) : ViewModel() {

    // 🔥 SINGLE SOURCE
    val callModel: StateFlow<CallModel?> = CallStore.call

    val uiState = MutableStateFlow(CallUiState())
    val error = MutableStateFlow<String?>(null)

    fun startCall(callerAccountId: Long, calleeAccountId: Long) {
        viewModelScope.launch {
            try {
                val call = startCallUseCase(callerAccountId, calleeAccountId)
                callManager.onOutgoing(call)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun acceptCall() {
        val call = CallStore.current() ?: return
        viewModelScope.launch {
            acceptCall(call.callId, call.callerAccountId, call.calleeAccountId)
            // RTM response will update CallStore
        }
    }

    fun rejectCall() {
        val call = callModel.value ?: return

        viewModelScope.launch {
            try {
                rejectCallUseCase(
                    callId = call.callId,
                    callerAccountId = call.callerAccountId,
                    calleeAccountId = call.calleeAccountId
                )

                // 🔥 LOCAL state update (DO NOT WAIT FOR RTM)
                callManager.onRejected()

            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }


    fun endCall() {
        val call = callModel.value ?: return

        viewModelScope.launch {
            try {
                endCallUseCase(
                    callId = call.callId,
                    callerAccountId = call.callerAccountId,
                    calleeAccountId = call.calleeAccountId,
                    callStatus = call.status
                )

                // 🔥 LOCAL first
                callManager.onEnded()

            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

}
