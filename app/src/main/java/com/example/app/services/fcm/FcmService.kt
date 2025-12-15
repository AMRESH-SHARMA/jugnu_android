package com.example.app.services.fcm

import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.app.MainActivity
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.utils.AppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var prefs: UserPreferencesRepository

    override fun onNewToken(token: String) {
        Log.d("RTM", "FCM NEW TOKEN = $token")
        CoroutineScope(Dispatchers.IO).launch {
            prefs.saveToken(token)  // TokenManager will sync to backend
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {

        Log.d("RTM", "FCM Received = ${message.data}")

        val type = message.data["event"] ?: return

        try {
            // Convert Map<String, String> → JSON string
            val jsonString = kotlinx.serialization.json.Json.encodeToString(
                message.data
            )

            // Decode into the SAME payload used by RTM
            val payload = kotlinx.serialization.json.Json.decodeFromString<CallSignalPayload>(
                jsonString
            )

            when (type) {

                // --------------------------------------------------------------
                // 1. INCOMING CALL (fallback when RTM unreachable)
                // --------------------------------------------------------------
                AppConstants.EVENT_INCOMING_CALL -> {

                    CallEventBus.emit(
                        CallEvent.Incoming(
                            callId = payload.callId,
                            callType = payload.callType,
                            callerAccountId = payload.callerAccountId,
                            calleeAccountId = payload.calleeAccountId,
                            channel = payload.channel
                        )
                    )
                }

                // --------------------------------------------------------------
                // 3. Call ended fallback
                // --------------------------------------------------------------
                AppConstants.EVENT_CALL_REJECTED,
                AppConstants.EVENT_CALL_ENDED -> {
                    Log.d("RTM", "FCM CALL ENDED (fallback)")
                    CallEventBus.emit(CallEvent.Ended(payload.callId))
                }

                // --------------------------------------------------------------
                // 4. Caller cancelled before callee answered
                // --------------------------------------------------------------
                AppConstants.EVENT_CALL_CANCELLED -> {
                    Log.d("RTM", "FCM CALL CANCELLED (fallback)")
                    CallEventBus.emit(CallEvent.Ended(payload.callId))
                }

            }

        } catch (e: Exception) {
            Log.e("RTM", "FCM Failed to parse FCM payload", e)
        }
    }

    // --------------------------------------------------------------
    // Wake MainActivity when app is backgrounded or killed
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
