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
    private val rtmCallSignaling: com.example.app.core.rtm.RtmCallSignaling,
    @ApplicationContext private val appContext: Context,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val TAG = "RTM"
    
    init {
        Log.d(TAG, "EventObserver: INIT - starting observers")
        
        // Start network tracking
        networkStateTracker.startTracking()
        
        observeCallEvents()
        observePresenceEvents()
        observeAppLifecycle()
        observeNetworkChanges()
        
        Log.d(TAG, "EventObserver: INIT complete - all observers started")
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
                        
                        // Send acknowledgment back to caller that we received the call
                        rtmCallSignaling.sendCallEvent(
                            channel = com.example.app.core.rtm.RtmChannels.user(event.callerAccountId),
                            payload = com.example.app.core.rtm.CallSignalPayload(
                                event = com.example.app.AppConstants.EVENT_CALL_RECEIVED,
                                callId = event.callId,
                                callType = event.callType,
                                callerAccountId = event.callerAccountId,
                                calleeAccountId = event.calleeAccountId
                            )
                        )
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
                        if (SessionManager.userAccountId == event.calleeAccountId) {
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
                Log.d(TAG, "EventObserver: Presence event received - $event")
                when (event) {

                    // ---- Socket connected → ONLINE (unless already BUSY)
                    is PresenceEvent.Connected -> {
                        presenceManager.onConnected()
                    }

                    // ---- Socket disconnected → OFFLINE (always)
                    is PresenceEvent.Disconnected -> {
                        presenceManager.onDisconnected()
                    }

                    // ---- Optional (future: server-driven state)
                    is PresenceEvent.StatusChanged -> {
                        presenceManager.onRemoteStateChanged(event.state)
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
                val isLoggedIn = userSession.isLoggedIn()
                val accountId = userSession.accountId
                val sessionId = userSession.sessionId
                
                Log.d(TAG, "EventObserver: App foreground state changed - isForeground=$isForeground, isLoggedIn=$isLoggedIn, accountId=$accountId, sessionId=$sessionId")
                
                if (isForeground) {
                    // Only connect WebSocket if user is logged in
                    if (isLoggedIn) {
                        Log.d(TAG, "EventObserver: App foreground + logged in → connecting WebSocket")
                        presenceManager.onAppForeground()
                    } else {
                        Log.d(TAG, "EventObserver: App foreground but NOT logged in → skipping WebSocket connect")
                    }
                } else {
                    Log.d(TAG, "EventObserver: App background → disconnecting WebSocket")
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
                val isLoggedIn = userSession.isLoggedIn()
                Log.d(TAG, "EventObserver: Network state changed - isAvailable=$isAvailable, isLoggedIn=$isLoggedIn")
                
                if (isAvailable) {
                    if (isLoggedIn) {
                        Log.d(TAG, "EventObserver: Network available + logged in → attempting reconnect")
                        presenceManager.onNetworkAvailable()
                    } else {
                        Log.d(TAG, "EventObserver: Network available but NOT logged in → skipping reconnect")
                    }
                } else {
                    Log.d(TAG, "EventObserver: Network lost → notifying presence manager")
                    presenceManager.onNetworkLost()
                }
            }
        }
    }
}