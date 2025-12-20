package com.example.app.core.rtm

import android.content.Context
import android.util.Log
import io.agora.rtm.ErrorInfo
import io.agora.rtm.PublishOptions
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmEventListener
import io.agora.rtm.SubscribeOptions
import kotlinx.coroutines.CoroutineScope

object RtmManager {
    private var rtmClient: RtmClient? = null
    private lateinit var eventListener: RtmEventListener
    private lateinit var scope: CoroutineScope

    private var currentUserId: String? = null
    fun init(
        context: Context,
        appId: String,
        userId: String,
        listener: RtmEventListener,
        appScope: CoroutineScope
    ) {
        scope = appScope
        eventListener = listener
        currentUserId = userId

        val config = RtmConfig.Builder(appId, userId)
            .eventListener(eventListener)
            .build()

        try {
            rtmClient = RtmClient.create(config)
            Log.d("RTM", "RTM client created")
        } catch (e: Exception) {
            Log.e("RTM", "Failed to create RTM client", e)
        }
    }

    fun login(token: String) {
        rtmClient?.login(token, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d("RTM", "RTM login success")
                // 🔑 SUBSCRIBE TO OWN CHANNEL
                val channelName = "account_$currentUserId"
                subscribe(
                    channelName = channelName,
                    callback = object : ResultCallback<Void?> {
                        override fun onSuccess(responseInfo: Void?) {
                            Log.d("RTM", "Subscribed to $channelName")
                        }

                        override fun onFailure(errorInfo: ErrorInfo) {
                            Log.e("RTM", "Subscribe failed: $errorInfo")
                        }
                    }
                )
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e("RTM", "RTM login failed: $errorInfo")
            }
        })
    }

    fun logout() {
        // 1️⃣ Logout (async, best-effort)
        rtmClient?.logout(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d("RTM", "RTM logout success")
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e("RTM", "RTM logout failed: $errorInfo")
            }
        })
        // 2️⃣ Remove listener (important)
        rtmClient?.removeEventListener(eventListener)

        // 3️⃣ Release RTM client (STATIC)
        RtmClient.release()

        // 4️⃣ Clear reference
        rtmClient = null
    }


    fun publish(
        channelName: String,
        message: String,
        callback: ResultCallback<Void?>
    ) {
        val options = PublishOptions()
        rtmClient?.publish(channelName, message, options, callback)
    }

    fun subscribe(channelName: String, callback: ResultCallback<Void?>) {
        val options = SubscribeOptions().apply {
            withMessage = true
        }
        rtmClient?.subscribe(channelName, options, callback)
    }

    fun unsubscribe(channelName: String, callback: ResultCallback<Void?>) {
        rtmClient?.unsubscribe(channelName, callback)
    }
}
