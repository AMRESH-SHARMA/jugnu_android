package com.example.app.core.network.data

import com.example.app.core.network.RtmTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Call FCM fallback
interface RtmAuthApi {
    @POST("rtm/token")
    suspend fun getRtmToken(@Body body: Map<String, Long>): RtmTokenResponse
}
