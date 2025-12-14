package com.example.app.feature.call.domain

data class CallUiState(
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val durationLabel: String = "00:00",
)

data class CallModel(
    val callId: String,
    val status: CallStatus,
    val channel: String? = null,          // nullable until accepted
    val callerAccountId: Long,
    val calleeAccountId: Long,
//    val calleeName: String?,
//    val calleeAvatar: String?
)

enum class CallStatus {
    IDLE,
    INCOMING_RINGING,
    OUTGOING_RINGING,
    CONNECTING,
    CONNECTED,
    ENDED
}