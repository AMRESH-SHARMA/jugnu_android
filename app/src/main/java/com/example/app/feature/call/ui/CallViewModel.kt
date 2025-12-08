package com.example.app.feature.call.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.usecase.AcceptCall
import com.example.app.feature.call.domain.usecase.RejectCall
import com.example.app.feature.call.domain.usecase.StartCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallUiState(
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val durationLabel: String = "00:00",
)

@HiltViewModel
class CallViewModel @Inject constructor(
    private val startCallUseCase: StartCall,
    private val acceptCallUseCase: AcceptCall,
    private val rejectCallUseCase: RejectCall
) : ViewModel() {

    val callState = MutableStateFlow<CallModel?>(null)
    val uiState = MutableStateFlow(CallUiState())
    val error = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null
    fun startCall(callerId: String, calleeId: String, channel: String) {
        viewModelScope.launch {
            try {
                callState.value = startCallUseCase(callerId, calleeId, channel)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun acceptCall() {
        val id = callState.value?.callId ?: return
        viewModelScope.launch {
            try {
                callState.value = acceptCallUseCase(id)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun rejectCall() {
        val id = callState.value?.callId ?: return
        viewModelScope.launch {
            try {
                callState.value = rejectCallUseCase(id)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun endCall() {
        stopTimer()
        rejectCall()
    }

    fun toggleMute() {
        uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun updateDuration(label: String) {
        uiState.update { it.copy(durationLabel = label) }
    }

    fun startTimer() {
        if (timerJob != null) return

        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000)
                seconds++
                updateDuration(formatSeconds(seconds))
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun formatSeconds(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }
}
