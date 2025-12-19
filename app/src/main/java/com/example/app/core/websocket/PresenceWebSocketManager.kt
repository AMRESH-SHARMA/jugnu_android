package com.example.app.core.websocket

import android.util.Log
import com.example.app.core.di.ApplicationScope
import com.example.app.core.session.UserSession
import com.example.app.utils.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceWebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val userSession: UserSession,
    @ApplicationScope private val scope: CoroutineScope
) {

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null

    private val isConnecting = AtomicBoolean(false)
    private val isConnected = AtomicBoolean(false)

    private var retryDelayMs = 1_000L
    private val maxRetryDelayMs = 30_000L

    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
    }

    // -------------------------
    // Public API
    // -------------------------

    fun connect() {
        Log.d("WS", "connect() loggedIn=${userSession.isLoggedIn()}")

        if (!userSession.isLoggedIn()) return
        if (isConnected.get() || isConnecting.get()) return

        isConnecting.set(true)

        val request = Request.Builder()
            .url(buildWsUrl())
            .addHeader("Authorization", "Bearer ${userSession.accountId}")
            .build()

        webSocket = okHttpClient.newWebSocket(request, socketListener)
    }

    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null

        stopHeartbeat()

        isConnecting.set(false)
        isConnected.set(false)

        webSocket?.close(1000, "Client disconnect")
        webSocket = null

        scope.launch {
            PresenceEventBus.events.emit(PresenceEvent.Disconnected)
        }
    }

    // -------------------------
    // Internal helpers
    // -------------------------

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

    private fun startHeartbeat(socket: WebSocket) {
        heartbeatJob?.cancel()

        heartbeatJob = scope.launch {
            while (isConnected.get()) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (!socket.send("ping")) break
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun buildWsUrl(): String =
        AppConstants.BASE_URL
            .trimEnd('/')
            .replace("https://", "wss://")
            .replace("http://", "ws://") +
                "/" + AppConstants.WS_PRESENCE_PATH.trimStart('/')

    // -------------------------
    // WebSocket listener
    // -------------------------

    private val socketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnecting.set(false)
            isConnected.set(true)

            resetBackoff()
            startHeartbeat(webSocket)

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Connected)
            }
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            isConnecting.set(false)
            isConnected.set(false)

            stopHeartbeat()

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            scheduleReconnect()
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            isConnecting.set(false)
            isConnected.set(false)

            stopHeartbeat()

            scope.launch {
                PresenceEventBus.events.emit(PresenceEvent.Disconnected)
            }

            scheduleReconnect()
        }
    }

    fun sendCallStarted() {
        webSocket?.send("""{"type":"CALL_START"}""")
    }

    fun sendCallEnded() {
        webSocket?.send("""{"type":"CALL_END"}""")
    }

}
