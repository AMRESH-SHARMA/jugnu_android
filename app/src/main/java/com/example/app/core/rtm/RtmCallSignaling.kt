package com.example.app.core.rtm

import android.util.Log
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RtmCallSignaling @Inject constructor() {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun sendCallEvent(
        channel: String,
        payload: CallSignalPayload,
        onFailure: (() -> Unit)? = null
    ) {
        val message = json.encodeToString(payload)

        RtmManager.publish(
            channelName = channel,
            message = message,
            callback = object : ResultCallback<Void?> {

                override fun onSuccess(responseInfo: Void?) {
                    // fire-and-forget (expected behavior)
                    Log.d(
                        "RTM",
                        "Published Event ${payload.event} → channel=$channel callId=${payload.callId}"
                    )
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    Log.e(
                        "RTM",
                        "Failed ${payload.event} → channel=$channel error=$errorInfo"
                    )
                    onFailure?.invoke()
                }
            }
        )
    }
}
