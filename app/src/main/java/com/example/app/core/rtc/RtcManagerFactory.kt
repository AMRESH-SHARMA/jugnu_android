package com.example.app.core.rtc

import com.example.app.core.call.CallType

interface RtcManagerFactory {
    fun create(type: CallType): RtcManager
}
