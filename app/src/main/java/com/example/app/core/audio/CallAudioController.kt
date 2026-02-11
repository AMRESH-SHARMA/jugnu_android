package com.example.app.core.audio

import com.example.app.core.call.CallStore
import com.example.app.core.di.ApplicationScope
import com.example.app.feature.call.domain.CallStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// ----------------------------------------------------------------
// Based on CallStore Updates it plays / stop appropriate audio
// ----------------------------------------------------------------
@Singleton
class CallAudioController @Inject constructor(
    private val audioPlayer: AudioPlayer,
    @ApplicationScope private val scope: CoroutineScope
) {

    // Prevent double-triggering
    private var lastStatus: CallStatus? = null

    init {
        scope.launch {
            CallStore.call.collect { call ->
                val status = call?.status
                
                // Call ended or cleared - always stop audio
                if (call == null) {
                    stopAll()
                    lastStatus = null
                    return@collect
                }
                
                // Ignore duplicate states
                if (status == lastStatus) return@collect
                lastStatus = status

                when (status) {

                    // 🔔 Incoming call ringtone
                    CallStatus.INCOMING_RINGING -> {
                        stopAll()
                        audioPlayer.play(AudioType.IncomingCall)
                    }

                    // 📞 Outgoing dialing tone
                    CallStatus.OUTGOING_RINGING -> {
                        stopAll()
                        audioPlayer.play(AudioType.OutgoingCall)
                    }

                    // 🔗 Joining RTC (stop tones)
                    CallStatus.CONNECTING -> {
                        stopAll()
                    }

                    // 🎤 Active call — no tones
                    CallStatus.CONNECTED -> {
                        stopAll()
                    }

                    // ❌ Terminal states
                    CallStatus.REJECTED,
                    CallStatus.CANCELLED,
                    CallStatus.ENDED -> {
                        stopAll()
                    }

                    // everything else → ignore
                    else -> Unit
                }
            }
        }
    }

    private fun stopAll() {
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
        }
    }
}
