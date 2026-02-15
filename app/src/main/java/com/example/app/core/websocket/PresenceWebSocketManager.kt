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

    companion object {
        private const val MSG_CALL_START = "CALL_START"
        private const val MSG_CALL_END = "CALL_END"
        private const val MSG_NET_OFFLINE = "NET_OFFLINE"
        private const val MSG_NET_ONLINE = "NET_ONLINE"
    }

    /** PUBLIC API **/
    fun connect() {
        if (!userSession.isLoggedIn()) {
            Log.e("RTM", "Cannot connect - user not logged in")
            return
        }
        if (isConnected.get() || isConnecting.get()) {
            Log.w("RTM", "Already connected or connecting")
            return
        }

        isConnecting.set(true)

        val accountId = userSession.accountId
        val role = userSession.role.name
        
        Log.d("RTM", "=== CONNECTING TO WEBSOCKET ===")
        Log.d("RTM", "AccountID: $accountId")
        Log.d("RTM", "Role: $role")
        Log.d("RTM", "URL: ${buildWsUrl()}")

        val request = Request.Builder()
            .url(buildWsUrl())
            .addHeader("Authorization", "Bearer $accountId")
            .addHeader("X-User-Role", role)
            .build()
        
        Log.d("RTM", "Authorization: Bearer $accountId")
        Log.d("RTM", "X-User-Role: $role")

        webSocket = okHttpClient.newWebSocket(request, socketListener)
    }

    fun disconnect() {
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
        webSocket?.send(MSG_CALL_START)
    }

    fun sendCallEnd() {
        webSocket?.send(MSG_CALL_END)
    }

    fun sendNetOffline() {
        webSocket?.send(MSG_NET_OFFLINE)
    }

    fun sendNetOnline() {
        webSocket?.send(MSG_NET_ONLINE)
    }

    /** INTERNAL **/
    private fun scheduleReconnect() {
        if (!userSession.isLoggedIn()) return
        if (reconnectJob?.isActive == true) return

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

        //TODO for debugging
//        fun onPong(webSocket: WebSocket, bytes: ByteString) {
//            Log.d("RTM", "PONG received from server")
//        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("RTM", "WS OPEN: $response")

            isConnecting.set(false)
            isConnected.set(true)

            reconnectJob?.cancel()  // ✅ Cancel pending reconnect
            reconnectJob = null
            resetBackoff()

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Connected)
            }
        }

        /**
         * onMessage: is triggered ONLY for text / binary messages not for ping pong control frames
         */
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.w("RTM", "WS onMesssage = $text")

            try {
                val obj = JSONObject(text)

                // ---- SNAPSHOT ----
                if (obj.optString("type") == "presence_snapshot") {
                    val snapshot = json.decodeFromString<PresenceSnapshotMessage>(text)

                    snapshot.data.forEach { (id, status) ->
                        val state = status.toPresenceState()
                        remotePresenceStore.update(id, state)

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
                Log.e("RTM", "WS decode error", e)
            }
        }


        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("RTM", "WS FAILURE = ${t.message}")

            isConnecting.set(false)
            isConnected.set(false)

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("RTM", "WS CLOSED $reason")

            isConnecting.set(false)
            isConnected.set(false)

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            scheduleReconnect()
        }
    }
}
