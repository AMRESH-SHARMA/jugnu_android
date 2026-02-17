package com.example.app.core.rtm

import android.util.Log
import com.example.app.AppConstants
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import io.agora.rtm.MessageEvent
import io.agora.rtm.RtmEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

// =========================
// It listens to RTM messages from the server/other device.
// Emit an internal CallEvent via CallEventBus
// =========================
class RtmEventListenerImpl(
    private val scope: CoroutineScope
) : RtmEventListener {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val acceptedCalls = ConcurrentHashMap.newKeySet<String>()


    override fun onMessageEvent(event: MessageEvent) {
        val rtmMessage = event.message
        val raw = rtmMessage.toString()
        Log.d(
            "RTM",
            "ON RTM Event Received $raw"
        )

        // 🔥 Extract JSON between "message: " and ", data:"
        val jsonPayload = raw
            .substringAfter("message: ")
            .substringBefore(", data:")

        try {
            val signal = json.decodeFromString<CallSignalPayload>(jsonPayload)

            scope.launch {
//                Log.d(
//                    "RTM",
//                    "RtmEventListenerImpl $signal"
//                )
                when (signal.event) {
                    AppConstants.EVENT_INCOMING_CALL -> {
                        CallEventBus.emit(
                            CallEvent.Incoming(
                                callId = signal.callId,
                                callType = signal.callType!!,
                                callerAccountId = signal.callerAccountId!!,
                                calleeAccountId = signal.calleeAccountId!!,
                                channel = signal.channel
                            )
                        )
                    }

                    AppConstants.EVENT_CALL_RECEIVED -> {
                        CallEventBus.emit(
                            CallEvent.CallReceived(signal.callId)
                        )
                    }

                    AppConstants.EVENT_CALL_ACCEPTED -> {
                        if (!acceptedCalls.add(signal.callId)) {
                            Log.w(
                                "RTM",
                                "Duplicate call_accepted ignored for callId=${signal.callId}"
                            )
                            return@launch
                        }

                        CallEventBus.emit(
                            CallEvent.Accepted(
                                callId = signal.callId,
                                channel = signal.channel!!,
                                rtcToken = signal.rtcToken!!
                            )
                        )
                    }

                    AppConstants.EVENT_CALL_REJECTED -> {
                        CallEventBus.emit(
                            CallEvent.Rejected(signal.callId)
                        )
                    }

                    AppConstants.EVENT_CALL_CANCELLED -> {
                        CallEventBus.emit(
                            CallEvent.Cancelled(signal.callId, signal.calleeAccountId)
                        )
                    }

                    AppConstants.EVENT_CALL_ENDED -> {
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
//        Log.w("RTM", "RTM token expired")
    }
}