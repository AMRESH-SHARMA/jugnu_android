package com.example.app.core.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_END_CALL) {
            val call = CallStore.current() ?: return
            CallEventBus.emit(CallEvent.Cancelled(call.callId))
        }
    }

    companion object {
        const val ACTION_END_CALL = "CALL_ACTION_END_CALL"
    }
}


