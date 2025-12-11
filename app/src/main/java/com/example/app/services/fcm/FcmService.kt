package com.example.app.services.fcm

import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.app.MainActivity
import com.example.app.core.user.repository.UserPreferencesRepository
import com.example.app.feature.call.ui.CallEvent
import com.example.app.feature.call.ui.CallEventBus
import com.example.app.utils.AppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var prefs: UserPreferencesRepository

    override fun onNewToken(token: String) {
        Log.d("FCM", "NEW TOKEN = $token")

        CoroutineScope(Dispatchers.IO).launch {
            prefs.saveToken(token)  // Store token, TokenManager will send
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Data = ${message.data}")

        val type = message.data["type"] ?: return

        when (type) {

            // --------------------------------------------------------------
            // 1. INCOMING CALL → callee opens call screen
            // --------------------------------------------------------------
            AppConstants.EVENT_INCOMING_CALL -> {
                sendEventToForeground(CallEvent.IncomingCall(message.data))

                navigateToMain(
                    route = AppConstants.EVENT_INCOMING_CALL,
                    extras = mapOf(
                        "callerId" to message.data["callerId"]?.toLong(),
                        "calleeId" to message.data["calleeId"]?.toLong(),
                        "callId" to message.data["callId"]
                    )
                )
            }

            // --------------------------------------------------------------
            // 2. CALL REJECTED → BOTH caller and callee go back to home
            // --------------------------------------------------------------
            AppConstants.EVENT_CALL_REJECTED -> {
                Log.d("FCM", "CALL REJECTED")

                sendEventToForeground(CallEvent.CallRejected(message.data["callId"]))

                navigateToMain(route = AppConstants.EVENT_CALL_REJECTED)
            }

            // --------------------------------------------------------------
            // 3. CALL ENDED → BOTH sides return to home
            // --------------------------------------------------------------
            AppConstants.EVENT_CALL_ENDED -> {
                Log.d("FCM", "CALL ENDED")

                sendEventToForeground(CallEvent.CallEnded(message.data["callId"]))

                navigateToMain(route = AppConstants.EVENT_CALL_ENDED)
            }

            // --------------------------------------------------------------
            // 4. CALL CANCELLED (caller ended before callee accepts)
            // --------------------------------------------------------------
            AppConstants.EVENT_CALL_CANCELLED -> {
                Log.d("FCM", "CALL CANCELLED")

                sendEventToForeground(CallEvent.CallRejected(message.data["callId"]))

                navigateToMain(route = AppConstants.EVENT_CALL_CANCELLED)
            }
        }
    }

    // --------------------------------------------------------------
    // Helper to emit events into the shared bus so UI can react
    // --------------------------------------------------------------
    private fun sendEventToForeground(event: CallEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            CallEventBus.send(event)
        }
    }

    // --------------------------------------------------------------
    // Launch the MainActivity for background-mode notifications
    // --------------------------------------------------------------
    private fun navigateToMain(route: String, extras: Map<String, Any?> = emptyMap()) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("route", route)

            extras.forEach { (k, v) ->
                when (v) {
                    is Long -> putExtra(k, v)
                    is Int -> putExtra(k, v)
                    is String -> putExtra(k, v)
                }
            }

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        startActivity(intent)
    }
}

//@AndroidEntryPoint
//class FcmService : FirebaseMessagingService() {
//
//    @Inject
//    lateinit var prefs: UserPreferencesRepository
//
//    override fun onNewToken(token: String) {
//        Log.d("FCM", "NEW TOKEN = $token")
//
//        CoroutineScope(Dispatchers.IO).launch {
//            prefs.saveToken(token)  // DO NOT send token here
//        }
//    }
//
//    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//    override fun onMessageReceived(message: RemoteMessage) {
//        Log.d("FCM", "Data = ${message.data}")
//
//        val type = message.data["type"] ?: return
//        when (type) {
//
//            AppConstants.EVENT_INCOMING_CALL -> {
//                navigateToMain(
//                    route = AppConstants.EVENT_INCOMING_CALL,
//                    extras = mapOf(
//                        "callerId" to message.data["callerId"]?.toLong(),
//                        "calleeId" to message.data["calleeId"]?.toLong(),
//                        "callId" to message.data["callId"]
//                    )
//                )
//            }
//
//            AppConstants.EVENT_CALL_REJECTED -> {
//                Log.d("FCM", "CALL REJECTED")
//                navigateToMain(route = AppConstants.EVENT_CALL_REJECTED)
//            }
//
//            AppConstants.EVENT_CALL_ENDED -> {
//                Log.d("FCM", "CALL ENDED")
//                navigateToMain(route = AppConstants.EVENT_CALL_ENDED)
//            }
//
//            AppConstants.EVENT_CALL_CANCELLED -> {
//                Log.d("FCM", "CALL CANCELLED")
//                navigateToMain(route = AppConstants.EVENT_CALL_CANCELLED)
//            }
//        }
//        // your call notification logic
//        // Based on notification open respective screen
//
//    }
//
//    private fun navigateToMain(route: String, extras: Map<String, Any?> = emptyMap()) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            putExtra("route", route)
//            extras.forEach { (k, v) ->
//                when (v) {
//                    is Long -> putExtra(k, v)
//                    is String -> putExtra(k, v)
//                    is Int -> putExtra(k, v)
//                }
//            }
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        startActivity(intent)
//    }
//}
