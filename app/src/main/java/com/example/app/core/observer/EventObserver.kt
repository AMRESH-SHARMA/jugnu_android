package com.example.app.core.observer

import android.content.Context
import android.util.Log
import com.example.app.core.audio.CallAudioController
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.CallManager
import com.example.app.core.call.notification.IncomingCallNotificationManager
import com.example.app.core.call.notification.MissedCallNotificationManager
import com.example.app.core.di.ApplicationScope
import com.example.app.core.rtc.CallRtcController
import com.example.app.core.session.SessionManager
import com.example.app.core.websocket.PresenceEvent
import com.example.app.core.websocket.PresenceEventBus
import com.example.app.core.websocket.PresenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/*
* EventObserver listens to Observes store / domain event buses and forwards them to the right domain managers.
* It does NOT make decisions. It only routes.
* does NOT update CallStore directly
* */

@Singleton
class EventObserver @Inject constructor(
    private val callManager: CallManager,
    private val presenceManager: PresenceManager,
    private val callAudioController: CallAudioController,  //Just inject to make sure CallAudioController must initialized
    private val callRtcController: CallRtcController,  //Just inject to make sure CallRtcController must initialized
    private val incomingCallNotificationManager: IncomingCallNotificationManager,
    private val missedCallNotificationManager: MissedCallNotificationManager,
    private val appForegroundTracker: AppForegroundTracker,
    private val networkStateTracker: NetworkStateTracker,
    private val userSession: com.example.app.core.session.UserSession,
    private val callRepository: com.example.app.feature.call.data.CallRepository,
    private val presenceWebSocketManager: com.example.app.core.websocket.PresenceWebSocketManager,
    @ApplicationContext private val appContext: Context,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val TAG = "APP:EVENTOBSERVER"
    private var started = false
    
    fun start() {
        if (started) return
        started = true
        
        Log.d(TAG, "EventObserver: Starting observers after session load")
        
        // Start network tracking
        networkStateTracker.startTracking()
        
        observeCallEvents()
        observePresenceEvents()
        observeWebSocketConnectionState()
        observeAppLifecycle()
        observeNetworkChanges()
        
        Log.d(TAG, "EventObserver: All observers started")
    }
    private fun observeCallEvents() {
        // -------------------------
        // Call events
        // -------------------------
        scope.launch {
            CallEventBus.events.collect { event ->
                Log.d(TAG, "EventObserver: Call event received - $event")
                when (event) {
                    is CallEvent.Outgoing -> {
                        callManager.onOutgoing(event)
                        presenceManager.onCallStarted()
                    }

                    is CallEvent.Incoming -> {
                        callManager.onIncoming(event)
                        presenceManager.onCallStarted()
                        
                        // Send acknowledgment to backend (required for watchdog)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            callRepository.callReceived(
                                callId = event.callId,
                                calleeAccountId = event.calleeAccountId
                            )
                        }
                    }

                    is CallEvent.CallReceived -> {
                        callManager.onCallReceived(event)
                    }

                    is CallEvent.Accepted -> {
                        callManager.onAccepted(event)
                    }

                    is CallEvent.Connected -> callManager.onConnected()

                    is CallEvent.Rejected -> {
                        /** callee rejects call without picking */
                        incomingCallNotificationManager.dismiss()
                        callManager.onRejected()
                        presenceManager.onCallEnded()
                    }

                    is CallEvent.Ended -> {
                        /** after picking call is ended */
                        incomingCallNotificationManager.dismiss()
                        callManager.onEnded()
                        presenceManager.onCallEnded()
                    }

                    is CallEvent.Cancelled -> {
                        /** Dismiss incoming call notification/ show missed call */
                        incomingCallNotificationManager.dismiss()
                        if (userSession.accountId == event.calleeAccountId) {
                            missedCallNotificationManager.showMissedCall()
                        }
                        callManager.onEnded()
                        presenceManager.onCallEnded()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun observePresenceEvents() {
        // -------------------------
        // Presence WebSocket events
        // -------------------------
        scope.launch {
            PresenceEventBus.events.collect { event ->
//                Log.d(TAG, "EventObserver: Presence event received - $event")
                when (event) {

                    // ---- Socket connected → ONLINE (unless already BUSY)
                    is PresenceEvent.Connected -> {
                        presenceManager.onConnected()
                    }

                    // ---- Socket disconnected → OFFLINE (always)
                    is PresenceEvent.Disconnected -> {
                        presenceManager.onDisconnected()
                    }

                    // ---- Remote status changes (for OTHER users, not self)
                    is PresenceEvent.StatusChanged -> {
                        val currentUserId = userSession.accountId.toString()
                        
                        // Only update local state if this is OUR status change
                        if (event.accountId == currentUserId) {
//                            Log.d(TAG, "EventObserver: Status change for SELF - updating local presence")
                            presenceManager.onRemoteStateChanged(event.state)
                        } else {
//                            Log.d(TAG, "EventObserver: Status change for OTHER user (${event.accountId}) - ignoring for local presence")
                            // Remote user status changes are already handled by RemotePresenceStore
                            // No need to update local presence
                        }
                    }
                }
            }
        }
    }

    private fun observeAppLifecycle() {
        // -------------------------
        // App foreground/background tracking
        // -------------------------
        scope.launch {
            appForegroundTracker.isForeground.collect { isForeground ->
                Log.d(TAG, "EventObserver: App foreground state changed - isForeground=$isForeground")
                
                if (isForeground) {
                    presenceManager.onAppForeground()
                } else {
                    presenceManager.onAppBackground()
                }
            }
        }
    }

    private fun observeNetworkChanges() {
        // -------------------------
        // Network availability tracking
        // -------------------------
        scope.launch {
            networkStateTracker.isNetworkAvailable.collect { isAvailable ->
//                Log.d(TAG, "EventObserver: Network state changed - isAvailable=$isAvailable")
                
                if (isAvailable) {
                    presenceManager.onNetworkAvailable()
                } else {
                    presenceManager.onNetworkLost()
                }
            }
        }
    }

    private fun observeWebSocketConnectionState() {
        // -------------------------
        // State-driven WebSocket connection management
        // Combines all conditions into single flow to avoid race conditions
        // -------------------------
        scope.launch {
            kotlinx.coroutines.flow.combine(
                userSession.sessionFlow,
                userSession.sessionIdFlow,
                networkStateTracker.isNetworkAvailable,
                appForegroundTracker.isForeground
            ) { (accountId, _), sessionId, networkAvailable, isForeground ->
                val isLoggedIn = accountId > 0 && sessionId.isNotBlank()
                isLoggedIn && networkAvailable && isForeground
            }
            .distinctUntilChanged()
            .collect { shouldConnect ->
//                Log.d(TAG, "EventObserver: WebSocket connection state changed - shouldConnect=$shouldConnect")
                
                if (shouldConnect) {
//                    Log.d(TAG, "EventObserver: Conditions met → connecting WebSocket")
                    presenceWebSocketManager.connect()
                } else {
//                    Log.d(TAG, "EventObserver: Conditions not met → disconnecting WebSocket")
                    presenceWebSocketManager.disconnect()
                }
            }
        }
    }
}