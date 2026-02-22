package com.example.app.services.fcm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.example.app.AppConstants
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallType
import com.example.app.core.call.PendingCallStore
import com.example.app.core.call.notification.IncomingCallNotificationManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.observer.AppForegroundTracker
import com.example.app.core.observer.ScreenStateTracker
import com.example.app.core.preferences.user.data.UserPreferencesRepository
import com.example.app.core.rtm.CallSignalPayload
import com.example.app.core.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var appForegroundTracker: AppForegroundTracker

    @Inject
    lateinit var screenStateTracker: ScreenStateTracker

    @Inject
    lateinit var incomingCallNotificationManager: IncomingCallNotificationManager

    @Inject
    lateinit var pendingCallStore: PendingCallStore

    @Inject
    lateinit var prefs: UserPreferencesRepository

    @Inject
    lateinit var callRepository: com.example.app.feature.call.data.CallRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onNewToken(token: String) {
        Log.d("RTM", "New FCM token: $token")
        appScope.launch(Dispatchers.IO) {
            prefs.saveFcmToken(token)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {

        Log.d("RTM", "ON FCM EVENT Received: ${message.data}")

        val event = message.data["event"] ?: return
        
        // Check notification permission (for showing notification only)
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        // Screen On and app in foreground - RTM should handle it, but emit event as fallback
        val appInForeground = appForegroundTracker.isForeground.value
        val screenOn = screenStateTracker.isScreenOn()
        if (event == AppConstants.EVENT_INCOMING_CALL &&
            appInForeground && screenOn
        ) {
            Log.d("RTM", "Foreground + screen ON → RTM should handle, but emitting event as fallback")
            return
            // Continue to emit event as fallback (RTM might fail/delay)
        }

        val payload = try {
            val json = Json.encodeToString(message.data)
            Json.decodeFromString<CallSignalPayload>(json)
        } catch (e: Exception) {
            Log.e("RTM", "Failed to parse payload", e)
            return
        }

        when (event) {

            // ----------------------------------------------------------
            // INCOMING CALL (background / killed)
            // ----------------------------------------------------------
            AppConstants.EVENT_INCOMING_CALL -> {
                // Persist minimal data for cold start with server timestamp
                val startedAtMillis = (payload.startedAt ?: (System.currentTimeMillis() / 1000)) * 1000
                pendingCallStore.save(
                    callId = payload.callId,
                    callType = payload.callType ?: CallType.VOICE,
                    callerAccountId = payload.callerAccountId ?: return,
                    startedAt = startedAtMillis
                )

                val callTypeText = when (payload.callType) {
                    CallType.VIDEO -> "Incoming video call"
                    CallType.VOICE -> "Incoming voice call"
                    null -> "Incoming call"
                }

                // Only show notification if permission is granted AND app is not in foreground
                if (hasNotificationPermission && !(appInForeground && screenOn)) {
                    incomingCallNotificationManager.showIncomingCall(
                        callId = payload.callId,
                        callType = callTypeText
                    )
                } else if (!hasNotificationPermission) {
                    Log.w("RTM", "POST_NOTIFICATIONS not granted, skipping notification (events still processed)")
                } else {
                    Log.d("RTM", "App in foreground, skipping notification (banner will show)")
                }

                // Send acknowledgment to backend (background/killed scenario)
                // Backend will notify caller via FCM
                appScope.launch(Dispatchers.IO) {
                    callRepository.callReceived(
                        callId = payload.callId,
                        calleeAccountId = SessionManager.userAccountId
                    )
                }

                // Emit local Incoming event (even without notification permission)
                CallEventBus.emit(
                    CallEvent.Incoming(
                        callId = payload.callId,
                        callerAccountId = payload.callerAccountId,
                        calleeAccountId = SessionManager.userAccountId,
                        callType = payload.callType ?: CallType.VOICE,
                        channel = payload.channel
                    )
                )
            }

            // ----------------------------------------------------------
            // CALL TERMINATION FALLBACKS
            // ----------------------------------------------------------
            AppConstants.EVENT_CALL_RECEIVED -> {
                // Callee's device received the call (sent by backend via FCM)
                CallEventBus.emit(
                    CallEvent.CallReceived(payload.callId)
                )
            }

            AppConstants.EVENT_CALL_REJECTED,
            AppConstants.EVENT_CALL_ENDED,
            AppConstants.EVENT_CALL_CANCELLED -> {
                // Stop ringing and dismiss incoming call notification
                incomingCallNotificationManager.dismiss()
                
                // Clear pending call data
                pendingCallStore.clear()
                
                // Emit event to update CallStore (stops audio if app is running)
                CallEventBus.emit(
                    CallEvent.Ended(payload.callId)
                )
            }
        }
    }
}
