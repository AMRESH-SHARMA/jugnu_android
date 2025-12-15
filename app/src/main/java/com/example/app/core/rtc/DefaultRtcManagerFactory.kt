package com.example.app.core.rtc

import com.example.app.core.call.CallType
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DefaultRtcManagerFactory @Inject constructor(
    private val voice: Provider<AgoraVoiceRtcManager>,
    private val video: Provider<AgoraVideoRtcManager>
) : RtcManagerFactory {

    override fun create(type: CallType): RtcManager {
        return when (type) {
            CallType.VOICE -> voice.get()
            CallType.VIDEO -> video.get()
        }
    }
}
