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

        // Android 13+ → notification permission guard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("RTM", "POST_NOTIFICATIONS not granted, ignoring notification")
            return
        }

        val event = message.data["event"] ?: return
        // Screen On and app in foreground ignore fcm
        val appInForeground = appForegroundTracker.isForeground.value
        val screenOn = screenStateTracker.isScreenOn()
        if (event == AppConstants.EVENT_INCOMING_CALL &&
            appInForeground && screenOn
        ) {
            Log.d("RTM", "Foreground + screen ON → ignore FCM")
            return
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
                // Persist minimal data for cold start
                pendingCallStore.save(
                    callId = payload.callId,
                    callType = payload.callType ?: CallType.VOICE,
                    callerAccountId = payload.callerAccountId ?: return
                )

                val callTypeText = when (payload.callType) {
                    CallType.VIDEO -> "Incoming video call"
                    CallType.VOICE -> "Incoming voice call"
                    null -> "Incoming call"
                }

                incomingCallNotificationManager.showIncomingCall(
                    callId = payload.callId,
                    callType = callTypeText
                )

                // 2️⃣ Emit local Incoming event (THIS IS THE KEY)
                CallEventBus.emit(
                    CallEvent.Incoming(
                        callId = payload.callId,
                        callerAccountId = payload.callerAccountId!!,
                        calleeAccountId = SessionManager.userAccountId,
                        callType = payload.callType ?: CallType.VOICE,
                        channel = payload.channel // optional
                    )
                )
            }

            // ----------------------------------------------------------
            // CALL TERMINATION FALLBACKS
            // ----------------------------------------------------------
            AppConstants.EVENT_CALL_REJECTED,
            AppConstants.EVENT_CALL_ENDED,
            AppConstants.EVENT_CALL_CANCELLED -> {

//                Log.d("RTM", "Call ended via FCM")
//                // 🔕 Stop ringing/Dismiss incoming call notification
////                        IncomingCallRingingService.stop(appContext)
////                        incomingCallNotificationManager.dismiss(event.callId)
////                IncomingCallRingingService.stop(applicationContext)
//
////                     2️⃣ Dismiss incoming call notification
//                incomingCallNotificationManager.dismiss(payload.callId)
//
////                     3️⃣ Clear pending cold-start call
//                pendingCallStore.clear()
//                CallEventBus.emit(
//                    CallEvent.Ended(payload.callId)
//                )
            }
        }
    }
}
