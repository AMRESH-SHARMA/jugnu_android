package com.example.app.feature.call.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.audio.AudioPlayer
import com.example.app.core.audio.AudioType
import com.example.app.core.call.CallManager
import com.example.app.core.call.CallStore
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
import com.example.app.core.rtc.AgoraEventListener
import com.example.app.core.rtc.RtcEvent
import com.example.app.core.rtc.RtcManager
import com.example.app.core.rtc.RtcManagerFactory
import com.example.app.core.session.SessionManager
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.domain.CallUiState
import com.example.app.feature.call.domain.usecase.AcceptCall
import com.example.app.feature.call.domain.usecase.EndCall
import com.example.app.feature.call.domain.usecase.RejectCall
import com.example.app.feature.call.domain.usecase.StartCall
import com.example.app.feature.user.data.UserRepository
import com.example.app.utils.AppConstants
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
    private val rtcManagerFactory: RtcManagerFactory,
    private val audioPlayer: AudioPlayer,
    private val userRepository: UserRepository
) : ViewModel(), AgoraEventListener {

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------

    /** 🔥 SINGLE SOURCE OF TRUTH */
    val callModel: StateFlow<CallModel?> = CallStore.call

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val error = MutableStateFlow<String?>(null)

    private var rtcManager: RtcManager? = null

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
    // TIMERS
    // ----------------------------------------------------------------

    private var timerJob: Job? = null
    private var connectTimeoutJob: Job? = null
    private var ringingTimeoutJob: Job? = null
    private val _headerUiState = MutableStateFlow(CallHeaderUiState())
    val headerUiState: StateFlow<CallHeaderUiState> =
        _headerUiState.asStateFlow()

    // ----------------------------------------------------------------
    // STATE REACTIONS (reactive safety net)
    // ----------------------------------------------------------------

    init {

        // --------------------------------------------------
        // 1️⃣ Call lifecycle side-effects (audio / rtc / timer)
        // --------------------------------------------------
        viewModelScope.launch {
            callStatus.collect { status ->
                when (status) {

                    CallStatus.INCOMING_RINGING -> {
                        audioPlayer.play(AudioType.IncomingCall)
                    }

                    // 🔔 Caller waiting for callee
                    CallStatus.OUTGOING_RINGING -> {
                        if (!audioPlayer.isPlaying()) {
                            audioPlayer.play(AudioType.OutgoingCall)
                        }
                        startRingingTimeout()
                    }

                    // 🔄 Joining RTC
                    CallStatus.CONNECTING -> {
                        audioPlayer.stop()
                        cancelRingingTimeout()
                        startConnectTimeout()
                    }

                    // ✅ Media connected
                    CallStatus.CONNECTED -> {
                        audioPlayer.stop()
                        cancelRingingTimeout()
                        cancelConnectTimeout()
                        startTimer()
                    }

                    // ❌ Call finished
                    CallStatus.ENDED -> {
                        cleanupSideEffects()
                    }

                    else -> Unit
                }
            }
        }

        // --------------------------------------------------
        // 2️⃣ Resolve header user (OUTGOING = local, INCOMING = API)
        // --------------------------------------------------
        viewModelScope.launch {
            callModel
                .collect { call ->

                    if (call == null) {
                        _headerUiState.value = CallHeaderUiState()
                        return@collect
                    }

                    // ✅ OUTGOING CALL → use local data
                    if (SessionManager.userId == call.callerAccountId &&
                        call.calleeName != null
                    ) {
                        _headerUiState.value = CallHeaderUiState(
                            name = call.calleeName,
                            avatarUrl = call.calleeAvatar,
//                            subtitle = callStatusToSubtitle(call.status),
                            isLoading = false
                        )
                        return@collect
                    }

                    // ✅ INCOMING CALL → fetch from API
                    val remoteUserId = call.callerAccountId

                    _headerUiState.value = CallHeaderUiState(
                        isLoading = true,
//                        subtitle = callStatusToSubtitle(call.status)
                    )

                    when (val result = userRepository.getCallerInfo(remoteUserId)) {

                        is ApiResult.Success -> {
                            _headerUiState.value = CallHeaderUiState(
                                name = result.data.name,
                                avatarUrl = result.data.avatar,
//                                subtitle = callStatusToSubtitle(call.status),
                                isLoading = false
                            )
                        }

                        is ApiResult.Error -> {
                            _headerUiState.value = CallHeaderUiState(
                                name = "Unknown",
                                avatarUrl = null,
//                                subtitle = callStatusToSubtitle(call.status),
                                isLoading = false
                            )
                        }
                    }
                }
        }
        // --------------------------------------------------
        // 3️⃣ Subtitle reacts ONLY to call status
        // --------------------------------------------------
//        viewModelScope.launch {
//            callStatus.collect { status ->
//                _headerUiState.update {
//                    it.copy(subtitle = callStatusToSubtitle(status))
//                }
//            }
//        }
    }


//    private fun callStatusToSubtitle(status: CallStatus?): String =
//        when (status) {
//            CallStatus.INCOMING_RINGING -> "Incoming call"
//            CallStatus.OUTGOING_RINGING -> "Calling…"
//            CallStatus.CONNECTED -> "Connected"
//            CallStatus.CONNECTING -> "Connecting…"
//            else -> ""
//        }

    // ----------------------------------------------------------------
    // CLEANUP (single source)
    // ----------------------------------------------------------------

    private fun cleanupSideEffects() {
        audioPlayer.stop()
        rtcManager?.leave()
        rtcManager = null
        cancelRingingTimeout()
        cancelConnectTimeout()
        stopTimer()
    }

    // ----------------------------------------------------------------
    // TIMEOUTS
    // ----------------------------------------------------------------

    private fun startRingingTimeout() {
        if (ringingTimeoutJob != null) return

        ringingTimeoutJob = viewModelScope.launch {
            delay(AppConstants.START_RINGING_TIMEOUT)

            if (callStatus.value == CallStatus.OUTGOING_RINGING) {
                cleanupSideEffects()
                endCall() // triggers RTM + backend
            }
        }
    }

    private fun cancelRingingTimeout() {
        ringingTimeoutJob?.cancel()
        ringingTimeoutJob = null
    }

    private fun startConnectTimeout() {
        if (connectTimeoutJob != null) return

        connectTimeoutJob = viewModelScope.launch {
            delay(AppConstants.START_CONNECT_TIMEOUT)
            if (callStatus.value == CallStatus.CONNECTING) {
                error.value = "Call connection timeout"
                cleanupSideEffects()
                callManager.onEnded()
            }
        }
    }

    private fun cancelConnectTimeout() {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = null
    }

    // ----------------------------------------------------------------
    // TIMER
    // ----------------------------------------------------------------

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

    fun startCall(
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long,
        calleeName: String,
        calleeAvatar: String?
    ) {
        viewModelScope.launch {
            try {
                val call = startCallUseCase(
                    callType,
                    callerAccountId,
                    calleeAccountId,
                    calleeName = calleeName,
                    calleeAvatar = calleeAvatar
                )
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
                    call.callType,
                    call.callerAccountId,
                    call.calleeAccountId
                )

                callManager.onConnecting()

                rtcManager = rtcManagerFactory.create(call.callType)
                collectRtcEvents(rtcManager!!)

                rtcManager!!.join(
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

        cleanupSideEffects()

        viewModelScope.launch {
            try {
                rejectCallUseCase(
                    call.callId,
                    call.callType,
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

        // 🔥 STOP LOCALLY FIRST
        cleanupSideEffects()
        callManager.onEnded()

        // 🔥 THEN backend + RTM
        viewModelScope.launch {
            try {
                endCallUseCase(
                    call.callId,
                    call.callerAccountId,
                    call.calleeAccountId,
                    call.status,
                    call.callType,
                )
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    // ----------------------------------------------------------------
    // RTC EVENTS
    // ----------------------------------------------------------------

    private fun collectRtcEvents(manager: RtcManager) {
        viewModelScope.launch {
            manager.events.collect { event ->
                when (event) {
                    RtcEvent.Connected -> callManager.onConnected()
                    RtcEvent.Disconnected -> {
                        cleanupSideEffects()
                        callManager.onEnded()
                    }

                    is RtcEvent.Error -> {
                        error.value = "RTC error ${event.code}"
                        cleanupSideEffects()
                        callManager.onEnded()
                    }

                    else -> {}
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // AUDIO CONTROLS
    // ----------------------------------------------------------------

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    override fun onCleared() {
        cleanupSideEffects()
        super.onCleared()
    }

    // ----------------------------------------------------------------
    // AGORA CALLBACKS (optional if using Flow)
    // ----------------------------------------------------------------

    override fun onConnected() {
        callManager.onConnected()
    }

    override fun onDisconnected() {
        cleanupSideEffects()
        callManager.onEnded()
    }

    override fun onError(errorCode: Int) {
        error.value = "RTC error: $errorCode"
        cleanupSideEffects()
        callManager.onEnded()
    }
}