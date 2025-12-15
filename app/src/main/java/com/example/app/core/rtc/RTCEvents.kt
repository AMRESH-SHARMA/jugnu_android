package com.example.app.core.rtc


sealed class RtcEvent {
    object Connected : RtcEvent()
    object Disconnected : RtcEvent()
    data class RemoteJoined(val uid: Int) : RtcEvent()
    data class RemoteLeft(val uid: Int) : RtcEvent()
    object CallEnded : RtcEvent()
    data class Error(val code: Int) : RtcEvent()
}
