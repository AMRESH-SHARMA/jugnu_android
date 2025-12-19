package com.example.app.services.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.app.MainActivity
import com.example.app.R

//TODO
object CallNotificationService {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showIncomingCallNotification(
        context: Context,
        data: Map<String, String>
    ) {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("callerId", data["callerId"])
            putExtra("callerName", data["callerName"])
            putExtra("avatar", data["avatar"])
            putExtra("isIncomingCall", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "call_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming call")
            .setContentText(data["callerName"])
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}
