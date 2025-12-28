package com.example.app.core.rtc


sealed class RtcEvent {
    object Connected : RtcEvent()
    object Disconnected : RtcEvent()
    object CallEnded : RtcEvent()
    data class RemoteJoined(val uid: Int) : RtcEvent()
    data class RemoteLeft(val uid: Int) : RtcEvent()
    data class Error(val code: Int) : RtcEvent()
}
