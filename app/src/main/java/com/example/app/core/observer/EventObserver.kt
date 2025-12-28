package com.example.app.core.observer

import android.util.Log
import com.example.app.core.audio.CallAudioController
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.rtc.CallRtcController
import com.example.app.core.websocket.PresenceEvent
import com.example.app.core.websocket.PresenceEventBus
import com.example.app.core.websocket.PresenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/*
* EventObserver listens to Observes store / domain event buses and forwards them to the right domain managers.
* It does NOT make decisions. It only routes.
* does NOT update CallStore directly
* */

@Singleton
class EventObserver @Inject constructor(
    private val callManager: CallManager,
    private val presenceManager: PresenceManager,
    private val callAudioController: CallAudioController,  //Just inject to make sure CallAudioController must initialized
    private val callRtcController: CallRtcController,  //Just inject to make sure CallRtcController must initialized
    @ApplicationScope private val scope: CoroutineScope
) {
    init {
        Log.w("RTM", "EventObserver INIT")
        // -------------------------
        // Call events
        // -------------------------
        scope.launch {
            CallEventBus.events.collect { event ->
                Log.w("RTM", "EventObserver received event=$event")
                when (event) {
                    is CallEvent.Outgoing -> {
                        callManager.onOutgoing(event)
                        presenceManager.onCallStarted()
                    }

                    is CallEvent.Incoming -> {
                        callManager.onIncoming(event)
                        presenceManager.onCallStarted()
                    }

                    is CallEvent.Accepted -> callManager.onAccepted(event)
                    is CallEvent.Connected -> callManager.onConnected()

                    is CallEvent.Rejected -> {
                        callManager.onRejected()
                        presenceManager.onCallEnded()
                    }


                    is CallEvent.Ended,
                    is CallEvent.Cancelled -> {
                        callManager.onEnded()
                        presenceManager.onCallEnded()
                    }
                }
            }
        }

        // -------------------------
        // Presence WebSocket events
        // -------------------------
        scope.launch {
            PresenceEventBus.events.collect { event ->
//                Log.d("RTM", "PresenceEvent received event=$event")
                when (event) {

                    // ---- Socket connected → ONLINE (unless already BUSY)
                    is PresenceEvent.Connected -> {
                        presenceManager.onConnected()
                    }

                    // ---- Socket disconnected → OFFLINE (always)
                    is PresenceEvent.Disconnected -> {
                        presenceManager.onDisconnected()
                    }

                    // ---- Optional (future: server-driven state)
                    is PresenceEvent.StatusChanged -> {
                        presenceManager.onRemoteStateChanged(event.state)
                    }
                }
            }
        }
    }
}