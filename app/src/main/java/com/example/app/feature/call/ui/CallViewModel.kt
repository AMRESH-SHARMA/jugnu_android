package com.example.app.feature.call.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.audio.AudioPlayer
import com.example.app.core.audio.AudioType
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallStore
import com.example.app.core.call.CallType
import com.example.app.core.network.ApiResult
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
    private val rtcManagerFactory: RtcManagerFactory,
    private val audioPlayer: AudioPlayer,
    private val userRepository: UserRepository
) : ViewModel() {

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

    private var callStarted = false
    private var timerJob: Job? = null
    private var connectTimeoutJob: Job? = null
    private var ringingTimeoutJob: Job? = null
    private var rtcEventsJob: Job? = null
    private var rtcJoinStarted = false
    private val _headerUiState = MutableStateFlow(CallHeaderUiState())
    val headerUiState: StateFlow<CallHeaderUiState> =
        _headerUiState.asStateFlow()
    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid
    private var warnedLowBalance = false
    private var balanceAtStart = 0     // seconds user can talk
    private var ratePerSec = 1         // cost rate

    // ----------------------------------------------------------------
    // STATE REACTIONS (reactive safety net)
    // ----------------------------------------------------------------

    init {

        // --------------------------------------------------
        // 1️⃣ Call lifecycle side-effects (audio / rtc / timer)
        // Observers the store, Reacts to store updates and trigger side-effects.
        // --------------------------------------------------
        viewModelScope.launch {
            CallStore.call.collect { call ->
                if (call == null) return@collect
                when (call.status) {

                    CallStatus.INCOMING_RINGING -> {
                        Log.w("RTM", "STATUS $call.status")
                        audioPlayer.play(AudioType.IncomingCall)
                    }

                    // 🔔 Caller waiting for callee
                    CallStatus.OUTGOING_RINGING -> {
                        Log.w("RTM", "STATUS $call.status")
                        if (!audioPlayer.isPlaying()) {
                            audioPlayer.play(AudioType.OutgoingCall)
                        }
                        startRingingTimeout()
                    }

                    // 🔄 Joining RTC
                    CallStatus.CONNECTING -> {
                        Log.w("RTM", "STATUS $call.status")
                        val call = CallStore.current() ?: return@collect
                        val channel = call.channel
                        val token = call.rtcToken

                        // ✅ 1. WAIT for RTC data
                        if (channel.isNullOrBlank() || token.isNullOrBlank()) {
                            Log.d("RTM", "RTC data not ready yet, waiting...")
                            return@collect
                        }

                        // ✅ 2. NOW lock
                        if (rtcJoinStarted) {
                            Log.w("RTM", "RTC join already started, skipping CONNECTING")
                            return@collect
                        }
                        rtcJoinStarted = true

                        // ✅ side-effects ONLY ONCE
                        audioPlayer.stop()
                        cancelRingingTimeout()
                        startConnectTimeout()

                        rtcManager = rtcManagerFactory.create(call.callType)
                        collectRtcEvents(rtcManager!!)
                        Log.w(
                            "RTM",
                            "RTC MANAGER CREATED → type=${call.callType} class=${rtcManager!!::class.java.simpleName}"
                        )

                        Log.d("RTM", "Joining channel=$channel token=$token")

                        rtcManager!!.join(
                            callId = call.callId,
                            channel = channel,
                            token = token
                        )
                    }


                    // ✅ Media connected
                    CallStatus.CONNECTED -> {
                        if (callStarted) return@collect
                        callStarted = true
                        Log.d("RTM", "Connected")
                        audioPlayer.stop()
                        cancelRingingTimeout()
                        cancelConnectTimeout()
                        // TODO
                        lowBalanceWarningTimer(
                            balance = 2,   // whatever value comes from backend
                            rate = 1
                        )
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
                            isLoading = false
                        )
                        return@collect
                    }

                    // ✅ INCOMING CALL → fetch from API
                    val remoteUserId = call.callerAccountId

                    _headerUiState.value = CallHeaderUiState(
                        isLoading = true
                    )

                    when (val result = userRepository.getCallerInfo(remoteUserId)) {

                        is ApiResult.Success -> {
                            _headerUiState.value = CallHeaderUiState(
                                name = result.data.name,
                                avatarUrl = result.data.avatar,
                                isLoading = false
                            )
                        }

                        is ApiResult.Error -> {
                            _headerUiState.value = CallHeaderUiState(
                                name = "Unknown",
                                avatarUrl = null,
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    // ----------------------------------------------------------------
    // CLEANUP (single source)
    // ----------------------------------------------------------------

    private fun cleanupSideEffects() {
        callStarted = false
        warnedLowBalance = false
        _remoteUid.value = null
        rtcJoinStarted = false
        rtcEventsJob?.cancel()
        rtcEventsJob = null

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
                val call = CallStore.current() ?: return@launch
                CallEventBus.emit(CallEvent.Cancelled(call.callId))
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
                CallEventBus.emit(CallEvent.Ended(CallStore.current()?.callId ?: return@launch))
//                callManager.onEnded()
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
        Log.d("RTM", "startTimer() called")

        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1_000)
                seconds++

                val remaining = balanceAtStart - (seconds * ratePerSec)
//                Log.d("RTM", "pre warn balanceAtStart $balanceAtStart, ratePerSec $ratePerSec")
                // 🔔 test beep after 3 seconds (trigger ONCE)
                if (!warnedLowBalance && seconds >= 2) {
                    Log.d("RTM", "warn")
                    warnedLowBalance = true
                    audioPlayer.play(AudioType.Beep)
                }

                // 🔔 low balance alert (trigger ONCE)
//                if (!warnedLowBalance && remaining in 1..15) {
//                    warnedLowBalance = true
//                    audioPlayer.play(AudioType.Beep)
//                }

                _uiState.update { state ->
                    state.copy(
                        elapsedSeconds = seconds,
                        remainingSeconds = remaining.coerceAtLeast(0),
                        durationLabel = formatDuration(seconds)
                    )
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

    fun lowBalanceWarningTimer(balance: Int, rate: Int) {
        balanceAtStart = balance
        ratePerSec = rate
        warnedLowBalance = false
    }

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
            when (val result = startCallUseCase(
                callType,
                callerAccountId,
                calleeAccountId,
                calleeName,
                calleeAvatar
            )) {
                is ApiResult.Success -> {
                    val call = result.data
                    CallEventBus.emit(
                        CallEvent.Outgoing(
                            callId = call.callId,
                            callerAccountId = call.callerAccountId,
                            calleeAccountId = call.calleeAccountId,
                            callType = call.callType,
                            calleeName = call.calleeName,
                            calleeAvatar = call.calleeAvatar
                        )
                    )
                }

                is ApiResult.Error -> {
                    error.value = result.message ?: "Unable to start call"
                }
            }
        }
    }

    fun acceptCall() {
        val call = CallStore.current() ?: return

        // 1️⃣ Optimistic local state
        CallEventBus.emit(
            CallEvent.Accepted(
                callId = call.callId,
                channel = null,
                rtcToken = ""
            )
        )
        // 1️⃣ Signal backend + trigger RTM `call_accepted`
        viewModelScope.launch {
            when (val result = acceptCallUseCase(
                call.callId,
                call.callType,
                call.callerAccountId,
                call.calleeAccountId
            )) {
                is ApiResult.Success -> {
                    val dto = result.data
                    // 3️⃣ Update CallStore with REAL RTC data (callee side)
                    CallEventBus.emit(
                        CallEvent.Accepted(
                            callId = call.callId,
                            channel = dto.channel,
                            rtcToken = dto.rtcToken
                        )
                    )
                }

                is ApiResult.Error -> {
                    error.value = result.message ?: "Accept failed"
                    onAcceptFailed(call, Throwable(result.message))
                }
            }
        }
    }

    fun rejectCall() {
        val call = CallStore.current() ?: return
        // 1️⃣ Local State update
        CallEventBus.emit(CallEvent.Rejected(call.callId))
        // 2️⃣ Side-Effects after
        cleanupSideEffects()
        // 3️⃣ Backend update
        viewModelScope.launch {
            when (val result = rejectCallUseCase(
                call.callId,
                call.callType,
                call.callerAccountId,
                call.calleeAccountId
            )) {
                is ApiResult.Error -> {
                    error.value = result.message ?: "Reject failed"
                }

                else -> Unit
            }
        }
    }

    fun endCall() {
        val call = CallStore.current() ?: return
        // 1️⃣ Local State update
        CallEventBus.emit(CallEvent.Cancelled(call.callId))
        // 2️⃣ Side-Effects after
        cleanupSideEffects()
        // 3️⃣ Backend update
        viewModelScope.launch {
            when (val result = endCallUseCase(
                call.callId,
                call.callerAccountId,
                call.calleeAccountId,
                call.status,
                call.callType
            )) {
                is ApiResult.Error -> {
                    error.value = result.message ?: "End call failed"
                }

                else -> Unit
            }
        }
    }


    private fun onAcceptFailed(call: CallModel, error: Throwable) {
        Log.e("RTM", "Accept failed", error)
        // 1️⃣ Roll back state
        CallEventBus.emit(CallEvent.Ended(call.callId))
        // 2️⃣ Cleanup side-effects
        cleanupSideEffects()
    }

    // ----------------------------------------------------------------
    // RTC EVENTS
    // ----------------------------------------------------------------

    private fun collectRtcEvents(manager: RtcManager) {
        if (rtcEventsJob != null) return   // ✅ prevent duplicate collectors

        rtcEventsJob = viewModelScope.launch {
            manager.events.collect { event ->
                when (event) {
                    RtcEvent.Connected -> {
                        val call = CallStore.current() ?: return@collect
                        if (call.status != CallStatus.CONNECTED) {
                            CallEventBus.emit(CallEvent.Connected(call.callId))
                        }
                    }

                    is RtcEvent.RemoteJoined -> {
                        // 🔥 THIS WAS MISSING
                        _remoteUid.value = event.uid
                    }

                    is RtcEvent.RemoteLeft -> {
                        _remoteUid.value = null
                    }

                    RtcEvent.Disconnected -> {
                        cleanupSideEffects()
                        endCall() // triggers RTM + backend
                    }

                    is RtcEvent.Error -> {
                        Log.d("RTM", "RTC error ${event.code}")
                        error.value = "RTC error ${event.code}"
                        cleanupSideEffects()
                        endCall() // triggers RTM + backend
                    }

                    else -> {}
                }
            }
        }
    }

    fun currentRtcManager(): RtcManager? = rtcManager

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
}