package com.example.app.core.network.data

import retrofit2.http.Body
import retrofit2.http.POST

data class NotifyFcmCallRequest(
    val callId: String,
    val callerId: Long,
    val calleeId: Long
)

// Call FCM fallback
interface CallNotificationApi {
    @POST("notify/fcm-call")
    suspend fun notifyCallViaFcm(@Body request: NotifyFcmCallRequest)
}
