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
import com.example.app.core.call.CallStore
import com.example.app.core.call.CallType
import com.example.app.core.call.PendingCallStore
import com.example.app.core.call.notification.IncomingCallNotificationManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.network.ApiResult
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
    lateinit var userSession: com.example.app.core.session.UserSession

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onNewToken(token: String) {
        Log.d("APP:FCM", "New FCM token: $token")
        appScope.launch(Dispatchers.IO) {
            prefs.saveFcmToken(token)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {

        Log.d("APP:FCM", "ON FCM EVENT Received: ${message.data}")

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
        val shouldSkipNotification = event == AppConstants.EVENT_INCOMING_CALL &&  appInForeground && screenOn
        if (shouldSkipNotification) {
            Log.d("APP:FCM", "Foreground + screen ON → RTM should handle, but emitting event as fallback")
            // Continue to emit event as fallback (RTM might fail/delay)
        }

        val payload = try {
            val json = Json { ignoreUnknownKeys = true }
            val jsonString = json.encodeToString(message.data)
            json.decodeFromString<CallSignalPayload>(jsonString)
        } catch (e: Exception) {
            Log.e("APP:FCM", "Failed to parse payload", e)
            return
        }

        when (event) {

            // ----------------------------------------------------------
            // INCOMING CALL
            // ----------------------------------------------------------
            AppConstants.EVENT_INCOMING_CALL -> {

                val calleeAccountId = userSession.accountId
                if (calleeAccountId <= 0) {
                    Log.w("APP:FCM", "Session not ready during FCM call")
                    return
                }

                // 🔥 1️⃣ If already handled via RTM → ignore duplicate FCM
                val existingCall = CallStore.current()
                if (existingCall?.callId == payload.callId) {
                    Log.d("APP:FCM", "Call already active via RTM, skipping FCM duplicate")
                    return
                }

                // 🔥 2️⃣ Ignore duplicate cold-start signal
                if (pendingCallStore.exists(payload.callId)) {
                    Log.d("APP:FCM", "Duplicate FCM call ignored")
                    return
                }

                val appInForeground = appForegroundTracker.isForeground.value
                val screenOn = screenStateTracker.isScreenOn()

                val startedAtMillis =
                    (payload.startedAt ?: (System.currentTimeMillis() / 1000)) * 1000

                // ------------------------------------------------------
                // FOREGROUND CASE
                // ------------------------------------------------------
                if (appInForeground) {

                    Log.d("APP:FCM", "Foreground FCM → emitting Incoming directly")

                    CallEventBus.emit(
                        CallEvent.Incoming(
                            callId = payload.callId,
                            callerAccountId = payload.callerAccountId ?: return,
                            calleeAccountId = calleeAccountId,
                            callType = payload.callType ?: CallType.VOICE,
                            channel = payload.channel
                        )
                    )

                    // Acknowledge backend (safe even if RTM also did)
                    appScope.launch(Dispatchers.IO) {
                        callRepository.callReceived(
                            callId = payload.callId,
                            calleeAccountId = calleeAccountId
                        )
                    }

                    return
                }

                // ------------------------------------------------------
                // BACKGROUND / KILLED CASE
                // ------------------------------------------------------

                Log.d("APP:FCM", "Background FCM → verifying call state with server")

                // Verify call state with server before showing notification
                appScope.launch(Dispatchers.IO) {
                    val stateResult = callRepository.getCallState(payload.callId)
                    
                    when (stateResult) {
                        is ApiResult.Success -> {
                            val callState = stateResult.data
                            
                            // Only show notification if call is still active
                            if (!callState.isActive || callState.isExpired) {
                                Log.w("APP:FCM", "Call not active on server: status=${callState.status}, isExpired=${callState.isExpired}")
                                return@launch
                            }
                            
                            Log.d("APP:FCM", "Call verified active → saving pending call + notification")
                            
                            pendingCallStore.save(
                                callId = payload.callId,
                                callType = payload.callType ?: CallType.VOICE,
                                callerAccountId = payload.callerAccountId ?: return@launch,
                                startedAt = startedAtMillis
                            )

                            val callTypeText = when (payload.callType) {
                                CallType.VIDEO -> "Incoming video call"
                                CallType.VOICE -> "Incoming voice call"
                                else -> "Incoming call"
                            }

                            if (hasNotificationPermission) {
                                incomingCallNotificationManager.showIncomingCall(
                                    callId = payload.callId,
                                    callType = callTypeText
                                )
                            } else {
                                Log.w("APP:FCM", "POST_NOTIFICATIONS not granted, skipping notification")
                            }

                            // Acknowledge backend
                            callRepository.callReceived(
                                callId = payload.callId,
                                calleeAccountId = calleeAccountId
                            )
                        }
                        
                        is ApiResult.Error -> {
                            Log.w("APP:FCM", "Failed to verify call state: ${stateResult.message}")
                            // Don't show notification if we can't verify with server
                        }
                    }
                }
            }

            // ----------------------------------------------------------
            // CALL RECEIVED (Caller side fallback)
            // ----------------------------------------------------------
            AppConstants.EVENT_CALL_RECEIVED -> {
                CallEventBus.emit(
                    CallEvent.CallReceived(payload.callId)
                )
            }

            // ----------------------------------------------------------
            // TERMINATION EVENTS
            // ----------------------------------------------------------
            AppConstants.EVENT_CALL_REJECTED,
            AppConstants.EVENT_CALL_ENDED,
            AppConstants.EVENT_CALL_CANCELLED -> {

                Log.d("APP:FCM", "Termination event received via FCM")

                incomingCallNotificationManager.dismiss()
                pendingCallStore.clear()

                CallEventBus.emit(
                    CallEvent.Ended(payload.callId)
                )
            }
        }
    }
}
