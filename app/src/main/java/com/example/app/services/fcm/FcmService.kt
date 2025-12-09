package com.example.app.services.fcm

import android.util.Log
import com.example.app.core.device.domain.SendDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var sendToken: SendDeviceTokenUseCase

    override fun onNewToken(token: String) {
        Log.d("FCM", "NEW TOKEN = $token")
//        DataStore.saveUserId("user-123")

        val userId = "2" // TODO load from datastore/auth

        CoroutineScope(Dispatchers.IO).launch {
            sendToken(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Data = ${message.data}")

        // your call notification logic
    }
}
