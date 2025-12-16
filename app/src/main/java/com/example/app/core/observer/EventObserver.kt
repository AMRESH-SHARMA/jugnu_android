package com.example.app.core.observer

import android.util.Log
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallManager
import com.example.app.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventObserver @Inject constructor(
    private val callManager: CallManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    init {
        scope.launch {
            CallEventBus.events.collect { event ->
                Log.d("RTM", "Observer received event=$event")
                when (event) {
                    is CallEvent.Outgoing -> callManager.onOutgoing(event)
                    is CallEvent.Incoming -> callManager.onIncoming(event)
                    is CallEvent.Accepted -> callManager.onAccepted(event)
                    is CallEvent.Connected -> callManager.onConnected()
                    is CallEvent.Rejected -> callManager.onRejected()
                    is CallEvent.Ended,
                    is CallEvent.Cancelled,
                    is CallEvent.Missed -> callManager.onEnded()
                }
            }
        }
    }
}