package com.example.app.core.call

import android.util.Log
import com.example.app.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallEventObserver @Inject constructor(
    private val callManager: CallManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    init {
        scope.launch {
            CallEventBus.events.collect { event ->
                Log.d("CALL", "Observer received event=$event")

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
