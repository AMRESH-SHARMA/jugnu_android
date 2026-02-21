package com.example.app.core.websocket

import android.util.Log
import com.example.app.AppConstants
import com.example.app.core.di.ApplicationScope
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.core.session.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PresenceWebSocketManager @Inject constructor(
    @Named("websocket") private val okHttpClient: OkHttpClient,
    private val userSession: UserSession,
    private val remotePresenceStore: RemotePresenceStore,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null

    private val isConnecting = AtomicBoolean(false)
    private val isConnected = AtomicBoolean(false)

    private var retryDelayMs = 1_000L
    private val maxRetryDelayMs = 30_000L
    
    private var shouldBeConnected = false // Track if we should maintain connection

    companion object {
        private const val TAG = "RTM"
        private const val MSG_CALL_START = "CALL_START"
        private const val MSG_CALL_END = "CALL_END"
        private const val MSG_NET_OFFLINE = "NET_OFFLINE"
        private const val MSG_NET_ONLINE = "NET_ONLINE"
    }

    /** PUBLIC API **/
    fun connect() {
        if (!userSession.isLoggedIn()) {
            Log.d(TAG, "WS connect() skipped - user not logged in")
            return
        }
        if (isConnected.get() || isConnecting.get()) {
            Log.d(TAG, "WS connect() skipped - already connected/connecting")
            return
        }

        shouldBeConnected = true
        isConnecting.set(true)

        val sessionId = userSession.sessionId
        val accountId = userSession.accountId
        val role = userSession.role.name

        Log.d(TAG, "WS connecting... sessionId=$sessionId, accountId=$accountId, role=$role")

        val request = Request.Builder()
            .url(buildWsUrl())
            .addHeader("Authorization", "Bearer $accountId")
            .addHeader("X-Session-ID", sessionId)
            .addHeader("X-Role", role)
            .build()

        webSocket = okHttpClient.newWebSocket(request, socketListener)
    }

    fun disconnect() {
        Log.d(TAG, "WS disconnect() called")
        
        shouldBeConnected = false
        reconnectJob?.cancel()

        isConnecting.set(false)
        isConnected.set(false)

        webSocket?.close(1000, "disconnect")
        webSocket = null

        scope.launch {
            PresenceEventBus.events.emit(PresenceEvent.Disconnected)
        }
    }

    fun sendCallStart() {
        val sent = webSocket?.send(MSG_CALL_START) ?: false
        Log.d(TAG, "WS sendCallStart() sent=$sent")
    }

    fun sendCallEnd() {
        val sent = webSocket?.send(MSG_CALL_END) ?: false
        Log.d(TAG, "WS sendCallEnd() sent=$sent")
    }

    fun sendNetOffline() {
        val sent = webSocket?.send(MSG_NET_OFFLINE) ?: false
        Log.d(TAG, "WS sendNetOffline() sent=$sent")
    }

    fun sendNetOnline() {
        val sent = webSocket?.send(MSG_NET_ONLINE) ?: false
        Log.d(TAG, "WS sendNetOnline() sent=$sent")
    }
    
    fun onAppBackground() {
        Log.d(TAG, "WS onAppBackground() - disconnecting")
        disconnect()
    }
    
    fun onAppForeground() {
        Log.d(TAG, "WS onAppForeground() - reconnecting")
        if (userSession.isLoggedIn()) {
            connect()
        }
    }
    
    fun onNetworkAvailable() {
        Log.d(TAG, "WS onNetworkAvailable() - reconnecting if needed")
        if (shouldBeConnected && !isConnected.get() && !isConnecting.get()) {
            connect()
        }
    }
    
    fun onNetworkLost() {
        Log.d(TAG, "WS onNetworkLost() - sending offline status")
        sendNetOffline()
    }

    /** INTERNAL **/
    private fun scheduleReconnect() {
        if (!userSession.isLoggedIn()) {
            Log.d(TAG, "WS scheduleReconnect() skipped - user not logged in")
            return
        }
        if (!shouldBeConnected) {
            Log.d(TAG, "WS scheduleReconnect() skipped - should not be connected")
            return
        }
        if (reconnectJob?.isActive == true) {
            Log.d(TAG, "WS scheduleReconnect() skipped - already scheduled")
            return
        }

        Log.d(TAG, "WS scheduleReconnect() in ${retryDelayMs}ms")
        reconnectJob = scope.launch {
            delay(retryDelayMs)
            retryDelayMs = (retryDelayMs * 2).coerceAtMost(maxRetryDelayMs)
            connect()
        }
    }

    private fun resetBackoff() {
        retryDelayMs = 1_000L
    }

    private fun buildWsUrl(): String {
        val base = (RemoteConfig.wsBaseUrl
            ?: RemoteConfig.apiBaseUrl.substringBefore("/api/v1"))
            .trimEnd('/')
            .replace("https://", "wss://")
            .replace("http://", "ws://")

        return base + "/" + AppConstants.WS_PRESENCE_PATH.trimStart('/')
    }


    /** SOCKET LISTENER **/

    private val socketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WS onOpen() - connection established, response code=${response.code}")

            isConnecting.set(false)
            isConnected.set(true)

            resetBackoff()

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Connected)
            }
        }

        /**
         * onMessage: is triggered ONLY for text / binary messages not for ping pong control frames
         */
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "WS onMessage() received: $text")

            try {
                val obj = JSONObject(text)

                // ---- DUPLICATE CONNECTION HANDLING ----
                if (obj.optString("type") == "connection_replaced") {
                    Log.w(TAG, "WS connection replaced by another session - closing old connection")
                    disconnect()
                    return
                }

                // ---- AVAILABILITY CHANGE ----
                if (obj.optString("type") == "availability_changed") {
                    val accountId = obj.getString("account_id")
                    val isAvailable = obj.getBoolean("is_available")
                    
                    Log.d(TAG, "WS availability update: accountId=$accountId, isAvailable=$isAvailable")
                    
                    remotePresenceStore.updateAvailability(accountId, isAvailable)
                    return
                }

                // ---- SNAPSHOT ----
                if (obj.optString("type") == "presence_snapshot") {
                    val snapshot = json.decodeFromString<PresenceSnapshotMessage>(text)
                    Log.d(TAG, "WS received presence snapshot with ${snapshot.data.size} users")

                    snapshot.data.forEach { (id, data) ->
                        // Update presence state
                        val state = data.status.toPresenceState()
                        remotePresenceStore.update(id, state)

                        // Update availability
                        remotePresenceStore.updateAvailability(id, data.is_available)

                        scope.launch {
                            PresenceEventBus.events.emit(
                                PresenceEvent.StatusChanged(
                                    accountId = id,
                                    state = state
                                )
                            )
                        }
                    }

                    return
                }

                // ---- SINGLE UPDATE ----
                val broadcast = json.decodeFromString<PresenceBroadcastMessage>(text)
                val state = broadcast.status.toPresenceState()
                
                Log.d(TAG, "WS presence update: accountId=${broadcast.account_id}, status=${broadcast.status}")

                remotePresenceStore.update(broadcast.account_id, state)

                scope.launch {
                    PresenceEventBus.events.emit(
                        PresenceEvent.StatusChanged(
                            accountId = broadcast.account_id,
                            state = state
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "WS onMessage() decode error: ${e.message}", e)
            }
        }


        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WS onFailure() - error: ${t.message}, response: ${response?.code}", t)

            isConnecting.set(false)
            isConnected.set(false)

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WS onClosed() - code=$code, reason=$reason")

            isConnecting.set(false)
            isConnected.set(false)

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            // Only reconnect if it was unexpected closure
            if (code != 1000) {
                Log.w(TAG, "WS unexpected closure (code=$code), scheduling reconnect")
                scheduleReconnect()
            }
        }
    }
}
