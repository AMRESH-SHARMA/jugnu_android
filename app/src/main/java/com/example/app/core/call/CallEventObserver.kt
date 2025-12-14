package com.example.app.core.call

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CallEventObserver(
    callManager: CallManager,
    scope: CoroutineScope
) {
    init {
        scope.launch {
            CallEventBus.events.collect { event ->
                when (event) {
                    is CallEvent.Incoming -> callManager.onIncoming(event)
                    is CallEvent.Accepted -> callManager.onAccepted(event)
                    is CallEvent.Rejected -> callManager.onRejected()
                    is CallEvent.Ended,
                    is CallEvent.Cancelled,
                    is CallEvent.Missed -> callManager.onEnded()
                }
            }
        }
    }
}
