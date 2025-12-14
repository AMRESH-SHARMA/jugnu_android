package com.example.app.feature.call.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.call.CallManager
import com.example.app.core.call.CallStore
import com.example.app.core.rtc.AgoraEventListener
import com.example.app.core.rtc.RtcEvent
import com.example.app.core.rtc.RtcManager
import com.example.app.core.session.SessionManager
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.domain.CallUiState
import com.example.app.feature.call.domain.usecase.AcceptCall
import com.example.app.feature.call.domain.usecase.EndCall
import com.example.app.feature.call.domain.usecase.RejectCall
import com.example.app.feature.call.domain.usecase.StartCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val startCallUseCase: StartCall,
    private val acceptCallUseCase: AcceptCall,
    private val rejectCallUseCase: RejectCall,
    private val endCallUseCase: EndCall,
    private val callManager: CallManager,
    private val rtcManager: RtcManager
) : ViewModel(), AgoraEventListener {

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------

    /** 🔥 SINGLE SOURCE OF TRUTH */
    val callModel: StateFlow<CallModel?> = CallStore.call

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val error = MutableStateFlow<String?>(null)

    val callStatus: StateFlow<CallStatus?> =
        callModel
            .map { it?.status }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    // ----------------------------------------------------------------
    // TIMER
    // ----------------------------------------------------------------

    private var timerJob: Job? = null
    private var connectTimeoutJob: Job? = null

    init {
        viewModelScope.launch {
            rtcManager.events.collect { event ->
                when (event) {

                    RtcEvent.Connected -> {
                        callManager.onConnected()
                    }

                    RtcEvent.Disconnected -> {
                        rtcManager.leave()          // 🔥 SAFETY
                        callManager.onEnded()
                    }

                    is RtcEvent.Error -> {
                        error.value = "RTC error ${event.code}"
                        rtcManager.leave()          // 🔥 SAFETY
                        callManager.onEnded()
                    }
                }
            }
        }
    }


    private fun startTimer() {
        if (timerJob != null) return

        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1_000)
                seconds++
                _uiState.update {
                    it.copy(durationLabel = formatDuration(seconds))
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun formatDuration(sec: Int): String =
        "%02d:%02d".format(sec / 60, sec % 60)

    // ----------------------------------------------------------------
    // USER ACTIONS
    // ----------------------------------------------------------------

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
            try {
                acceptCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId
                )

                callManager.onConnecting()

                rtcManager.join(
                    channel = call.channel!!,
                    uid = SessionManager.userId.toInt()
                )
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun rejectCall() {
        val call = CallStore.current() ?: return

        viewModelScope.launch {
            try {
                rejectCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId
                )
                callManager.onRejected()
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun endCall() {
        val call = CallStore.current() ?: return

        viewModelScope.launch {
            try {
                endCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId,
                    call.status
                )
                callManager.onEnded()
                rtcManager.leave()
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    // ----------------------------------------------------------------
    // AUDIO CONTROLS (UI ONLY)
    // ----------------------------------------------------------------

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    // ----------------------------------------------------------------
    // AGORA CALLBACKS
    // ----------------------------------------------------------------

    override fun onConnected() {
        callManager.onConnected()
    }

    override fun onDisconnected() {
        callManager.onEnded()
    }

    override fun onError(errorCode: Int) {
        error.value = "RTC error: $errorCode"
        callManager.onEnded()
    }
}


/*
@HiltViewModel
class CallViewModel @Inject constructor(
    private val startCallUseCase: StartCall,
    private val acceptCallUseCase: AcceptCall,
    private val rejectCallUseCase: RejectCall,
    private val endCallUseCase: EndCall,
    private val callManager: CallManager,
    private val rtcManager: RtcManager
) : ViewModel(), AgoraEventListener {

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------

    /** 🔥 SINGLE SOURCE OF TRUTH */
    val callModel: StateFlow<CallModel?> = CallStore.call

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val error = MutableStateFlow<String?>(null)

    val callStatus: StateFlow<CallStatus?> =
        callModel
            .map { it?.status }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    // ----------------------------------------------------------------
    // TIMER
    // ----------------------------------------------------------------

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            callStatus.collect { status ->
                when (status) {
                    CallStatus.CONNECTED -> startTimer()
                    CallStatus.ENDED -> stopTimer()
                    else -> Unit
                }
            }
        }
    }

    private fun startTimer() {
        if (timerJob != null) return

        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1_000)
                seconds++
                _uiState.update {
                    it.copy(durationLabel = formatDuration(seconds))
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun formatDuration(sec: Int): String =
        "%02d:%02d".format(sec / 60, sec % 60)

    // ----------------------------------------------------------------
    // USER ACTIONS
    // ----------------------------------------------------------------

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
            try {
                acceptCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId
                )

                callManager.onConnecting()

                rtcManager.join(
                    channel = call.channel!!,
                    uid = SessionManager.userId.toInt()
                )
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun rejectCall() {
        val call = CallStore.current() ?: return

        viewModelScope.launch {
            try {
                rejectCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId
                )
                callManager.onRejected()
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun endCall() {
        val call = CallStore.current() ?: return

        viewModelScope.launch {
            try {
                endCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId,
                    call.status
                )
                callManager.onEnded()
                rtcManager.leave()
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    // ----------------------------------------------------------------
    // AUDIO CONTROLS (UI ONLY)
    // ----------------------------------------------------------------

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    // ----------------------------------------------------------------
    // AGORA CALLBACKS
    // ----------------------------------------------------------------

    override fun onConnected() {
        callManager.onConnected()
    }

    override fun onDisconnected() {
        callManager.onEnded()
    }

    override fun onError(errorCode: Int) {
        error.value = "RTC error: $errorCode"
        callManager.onEnded()
    }
}


/*
@HiltViewModel
class CallViewModel @Inject constructor(
    private val startCallUseCase: StartCall,
    private val acceptCall: AcceptCall,
    private val rejectCallUseCase: RejectCall,
    private val endCallUseCase: EndCall,
    private val callManager: CallManager   // or CallActionCoordinator
    private val rtcManager: RtcManager
) : ViewModel(), AgoraEventListener {

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
            // 🔥 move to CONNECTING immediately
            callManager.onConnecting(call.callId)

            rtcManager.join(
                channel = call.channel!!,
                uid = SessionManager.userId.toInt()
            )
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

    val callStatus = callModel
        .map { it?.status }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val durationLabel = uiState
        .map { it.durationLabel }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "00:00"
        )

    val audioUiState = uiState
        .map { it.isMuted to it.isSpeakerOn }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false to false
        )

    fun toggleMute() {
        uiState.update {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeaker() {
        uiState.update {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    // Agora RTC
    fun onConnected() {
        callManager.onConnected()
    }

    fun onDisconnected() {
        callManager.onEnded()
    }

    fun onError(errorCode: Int) {
        kotlin.error.value = "RTC error: $errorCode"
        callManager.onEnded()
    }

}
*/