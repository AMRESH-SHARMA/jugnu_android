package com.example.app.core.rtc

sealed class RtcEvent {
    object Connected : RtcEvent()
    object Disconnected : RtcEvent()
    data class Error(val code: Int) : RtcEvent()
}
