package com.example.app.core.rtm

import android.util.Log
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.utils.AppConstants
import io.agora.rtm.MessageEvent
import io.agora.rtm.RtmEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class RtmEventListenerImpl(
    private val scope: CoroutineScope
) : RtmEventListener {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun onMessageEvent(event: MessageEvent) {
        val rtmMessage = event.message
        val raw = rtmMessage.toString()

        // 🔥 Extract JSON between "message: " and ", data:"
        val jsonPayload = raw
            .substringAfter("message: ")
            .substringBefore(", data:")

        Log.d("RTM", "RTM payload = $jsonPayload")

        try {
            val signal = json.decodeFromString<CallSignalPayload>(jsonPayload)

            Log.d(
                "RTM",
                "Event Received ${signal.event} callId=${signal.callId}"
            )

            scope.launch {
                when (signal.event) {
                    AppConstants.EVENT_INCOMING_CALL -> {
                        CallEventBus.emit(
                            CallEvent.Incoming(
                                callId = signal.callId,
                                callerAccountId = signal.callerAccountId,
                                calleeAccountId = signal.calleeAccountId,
                                channel = signal.channel,
                                callType = signal.callType
                            )
                        )
                    }

                    AppConstants.EVENT_CALL_ACCEPTED -> {
                        CallEventBus.emit(
                            CallEvent.Accepted(
                                callId = signal.callId,
                                channel = signal.channel
                            )
                        )
                    }

                    AppConstants.EVENT_CALL_REJECTED -> {
                        CallEventBus.emit(
                            CallEvent.Rejected(signal.callId)
                        )
                    }

                    AppConstants.EVENT_CALL_ENDED,
                    AppConstants.EVENT_CALL_CANCELLED -> {
                        CallEventBus.emit(
                            CallEvent.Ended(signal.callId)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "RTM",
                "Failed to decode RTM payload = $jsonPayload",
                e
            )
        }
    }

    fun onTokenExpired() {
        Log.w("RTM", "RTM token expired")
    }
}