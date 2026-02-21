package com.example.app.feature.call.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallStore
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiErrorHandler
import com.example.app.core.network.ApiResult
import com.example.app.core.rtc.CallRtcController
import com.example.app.core.rtc.VideoRenderer
import com.example.app.core.session.SessionManager
import com.example.app.core.ui.SnackbarManager
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus
import com.example.app.feature.call.domain.CallUiState
import com.example.app.feature.call.domain.usecase.AcceptCall
import com.example.app.feature.call.domain.usecase.EndCall
import com.example.app.feature.call.domain.usecase.RejectCall
import com.example.app.feature.call.domain.usecase.StartCall
import com.example.app.feature.user.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val userRepository: UserRepository,
    private val callRtcController: CallRtcController
) : ViewModel() {

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------
    val videoRenderer: StateFlow<VideoRenderer?> =
        callRtcController.videoRenderer

    val callModel: StateFlow<CallModel?> = CallStore.call

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val error = MutableStateFlow<String?>(null)

    // UI-only RTC info (who joined)
    val remoteUid: StateFlow<Int?> = callRtcController.remoteUid

    // header (name / avatar)
    private val _headerUiState = MutableStateFlow(CallHeaderUiState())
    val headerUiState: StateFlow<CallHeaderUiState> = _headerUiState.asStateFlow()

    // Cache caller info per call session to avoid repeated API calls
    private var cachedCallId: String? = null
    private var cachedCallerInfo: Pair<String, String?>? = null

    // Navigation event for insufficient balance
    private val _navigateToWallet = MutableSharedFlow<Unit>()
    val navigateToWallet: SharedFlow<Unit> = _navigateToWallet.asSharedFlow()

    // ----------------------------------------------------------------
    // UI TIMERS (presentation only)
    // ----------------------------------------------------------------

    private var timerJob: Job? = null
    private var warnedLowBalance = false
    private var balanceAtStart = 0
    private var ratePerSec = 1

    init {

        // --------------------------------------------------
        // Resolve header user
        // --------------------------------------------------
        viewModelScope.launch {
            callModel.collect { call ->
                if (call == null) {
                    _headerUiState.value = CallHeaderUiState()
                    // Clear cache when call ends
                    cachedCallId = null
                    cachedCallerInfo = null
                    return@collect
                }

                // outgoing -> use local cached callee info
                if (SessionManager.userAccountId == call.callerAccountId &&
                    call.calleeName != null
                ) {
                    _headerUiState.value = CallHeaderUiState(
                        name = call.calleeName,
                        avatarUrl = call.calleeAvatar,
                        isLoading = false
                    )
                    return@collect
                }

                // incoming -> check cache first
                if (call.callId == cachedCallId && cachedCallerInfo != null) {
                    _headerUiState.value = CallHeaderUiState(
                        name = cachedCallerInfo!!.first,
                        avatarUrl = cachedCallerInfo!!.second,
                        isLoading = false
                    )
                    return@collect
                }

                // incoming -> load caller (only once per call)
                _headerUiState.value = CallHeaderUiState(isLoading = true)

                when (val result = userRepository.getCallerInfo()) {
                    is ApiResult.Success -> {
                        // Cache the result
                        cachedCallId = call.callId
                        cachedCallerInfo = result.data.name to result.data.avatar
                        
                        _headerUiState.value = CallHeaderUiState(
                            name = result.data.name,
                            avatarUrl = result.data.avatar,
                            isLoading = false
                        )
                    }

                    is ApiResult.Error ->
                        _headerUiState.value = CallHeaderUiState(
                            name = "Unknown",
                            avatarUrl = null,
                            isLoading = false
                        )
                }
            }
        }

        // --------------------------------------------------
        // Start / stop UI duration timer only when BOTH connected and remote present
        // --------------------------------------------------
        viewModelScope.launch {
            combine(CallStore.call, remoteUid) { call, uid ->
                call?.status to uid
            }.collect { (status, uid) ->

                when {
                    status == CallStatus.CONNECTED && uid != null -> startTimer()

                    status in listOf(
                        CallStatus.CANCELLED,
                        CallStatus.REJECTED,
                        CallStatus.ENDED
                    ) -> stopTimer()
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // TIMER (UI Only)
    // ----------------------------------------------------------------

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1_000)
                seconds++

                val remaining = balanceAtStart - (seconds * ratePerSec)

                if (!warnedLowBalance && seconds >= 3) {
                    warnedLowBalance = true
                }

                _uiState.update {
                    it.copy(
                        elapsedSeconds = seconds,
                        remainingSeconds = remaining.coerceAtLeast(0),
                        durationLabel = "%02d:%02d".format(seconds / 60, seconds % 60)
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun lowBalanceWarningTimer(balance: Int, rate: Int) {
        balanceAtStart = balance
        ratePerSec = rate
        warnedLowBalance = false
    }

    // ----------------------------------------------------------------
    // USER ACTIONS (emit intent → engine reacts)
    // ----------------------------------------------------------------

    fun startCall(
        callType: CallType,
        callerAccountId: Long,
        calleeAccountId: Long,
        calleeName: String,
        calleeAvatar: String?
    ) {
        // Generate temporary callId for optimistic UI
        val tempCallId = java.util.UUID.randomUUID().toString()
        
        // 1️⃣ Emit event IMMEDIATELY (optimistic UI)
        CallEventBus.emit(
            CallEvent.Outgoing(
                tempCallId,
                callerAccountId,
                calleeAccountId,
                callType,
                calleeName,
                calleeAvatar
            )
        )
        
        // 2️⃣ Make API call in background
        viewModelScope.launch {
            when (val result = startCallUseCase(
                callType, callerAccountId, calleeAccountId, calleeName, calleeAvatar
            )) {
                is ApiResult.Success -> {
                    val call = result.data
                    // Update only callId and channel, keep status as OUTGOING_CONNECTING
                    // Status will change to OUTGOING_RINGING when we receive CallReceived event
                    val current = CallStore.current()
                    if (current != null && current.callId == tempCallId) {
                        CallStore.set(
                            current.copy(
                                callId = call.callId,
                                channel = call.channel
                            )
                        )
                    }
                }

                is ApiResult.Error -> {
                    val errorMessage = result.message ?: "Unable to start call"
                    
                    // Hangup the call
                    CallEventBus.emit(CallEvent.Ended(tempCallId))
                    
                    // Check if it's an insufficient balance error
                    if (result.exception != null && ApiErrorHandler.isInsufficientBalance(result.exception)) {
                        SnackbarManager.showError(errorMessage, duration = 4000L)
                        // Trigger navigation to wallet after a short delay
                        viewModelScope.launch {
                            delay(500)
                            _navigateToWallet.emit(Unit)
                        }
                    } else {
                        // Regular error handling
                        SnackbarManager.showError(errorMessage)
                    }
                    
                    error.value = errorMessage
                }
            }
        }
    }

    fun acceptCall() {
        val call = CallStore.current() ?: return

        // optimistic accept
        CallEventBus.emit(CallEvent.Accepted(call.callId, null, ""))

        viewModelScope.launch {
            when (val result = acceptCallUseCase(
                call.callId, call.callType, call.callerAccountId, call.calleeAccountId
            )) {
                is ApiResult.Success ->
                    CallEventBus.emit(
                        CallEvent.Accepted(
                            call.callId,
                            result.data.channel,
                            result.data.rtcToken
                        )
                    )

                is ApiResult.Error -> {
                    error.value = result.message ?: "Accept failed"
                    CallEventBus.emit(CallEvent.Ended(call.callId))
                }
            }
        }
    }

    fun rejectCall() {
        val call = CallStore.current() ?: return
        CallEventBus.emit(CallEvent.Rejected(call.callId))

        viewModelScope.launch {
            val res = rejectCallUseCase(
                call.callId, call.callType, call.callerAccountId, call.calleeAccountId
            )
            if (res is ApiResult.Error) error.value = res.message ?: "Reject failed"
        }
    }

    fun endCall() {
        val call = CallStore.current() ?: return

        CallEventBus.emit(CallEvent.Cancelled(call.callId, call.calleeAccountId))

        viewModelScope.launch {
            val res = endCallUseCase(
                call.callId,
                call.callerAccountId,
                call.calleeAccountId,
                call.status,
                call.callType
            )
            if (res is ApiResult.Error) error.value = res.message ?: "End call failed"
        }
    }

    // ----------------------------------------------------------------
    // UI AUDIO / SPEAKER TOGGLES (state only)
    // ----------------------------------------------------------------

    fun toggleMute() {
        val newState = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newState) }
        callRtcController.setMuted(newState)
    }

    fun toggleSpeaker() {
        val newState = !_uiState.value.isSpeakerOn
        _uiState.update { it.copy(isSpeakerOn = newState) }
        callRtcController.setSpeaker(newState)
    }

//    fun switchCamera() {
//        callRtcController.switchCamera()
//    }

}
