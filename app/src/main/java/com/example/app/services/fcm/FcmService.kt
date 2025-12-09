package com.example.app.services.fcm

import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.app.MainActivity
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Data = ${message.data}")

        // your call notification logic
        // Based on notification open respective screen
        if (message.data["type"] == "incoming_call") {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("callerId", message.data["callerId"]?.toLong())
                putExtra("calleeId", message.data["calleeId"]?.toLong())
                putExtra("callId", message.data["callId"])
                putExtra("route", "incoming_call")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        if (message.data["type"] == "call_ended") {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("route", "call_ended")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        if (message.data["type"] == "call_rejected") {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("route", "call_ended")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

    }
}
