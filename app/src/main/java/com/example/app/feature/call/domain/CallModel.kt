package com.example.app.feature.call.domain

import com.example.app.core.call.CallType

data class CallUiState(
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val durationLabel: String = "00:00",
    // ⏱ internal call timing
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int = 0,
)

data class CallModel(
    val callId: String,
    val status: CallStatus,
    val callType: CallType,
    val channel: String? = null,
    val callerAccountId: Long,
    val calleeAccountId: Long,
    val calleeName: String? = null,
    val calleeAvatar: String? = null,
    val rtcToken: String? = null
)

enum class CallStatus {
    INCOMING_RINGING,
    OUTGOING_RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
    CANCELLED,
    REJECTED
}