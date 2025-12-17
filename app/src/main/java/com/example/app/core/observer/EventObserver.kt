package com.example.app.core.observer

import android.util.Log
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.websocket.PresenceEvent
import com.example.app.core.websocket.PresenceEventBus
import com.example.app.core.websocket.PresenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/*
* Observes store and decides how state should change
* */

@Singleton
class EventObserver @Inject constructor(
    private val callManager: CallManager,
    private val presenceManager: PresenceManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    init {
        // -------------------------
        // Call events
        // -------------------------
        scope.launch {
            CallEventBus.events.collect { event ->
                Log.d("RTM", "Observer received event=$event")
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
                    is CallEvent.Cancelled,
                    is CallEvent.Missed -> {
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
                Log.d("WS", "PresenceEvent received event=$event")
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