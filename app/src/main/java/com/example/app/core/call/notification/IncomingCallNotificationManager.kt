package com.example.app.core.call.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.app.MainActivity
import com.example.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingCallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "incoming_call_channel"
        private const val CHANNEL_NAME = "Incoming Calls"
    }

    // ------------------------------------------------------------
    // SHOW INCOMING CALL NOTIFICATION (FCM / background-safe)
    // ------------------------------------------------------------
    fun showIncomingCall(
        callId: String,
        callType: String
    ) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannelIfNeeded(manager)

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("callId", callId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lockphone)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(callType)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenIntent, true)
            .setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            )
            .build()

        manager.notify(callId.hashCode(), notification)
    }

    // ------------------------------------------------------------
    // FOREGROUND SERVICE NOTIFICATION (ringing / ongoing call)
    // ------------------------------------------------------------
    fun buildForegroundNotification(
        callId: String,
        callType: String
    ): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lockphone)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(callType)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    // ------------------------------------------------------------
    // DISMISS
    // ------------------------------------------------------------
    fun dismiss(callId: String) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(callId.hashCode())
    }

    // ------------------------------------------------------------
    // CHANNEL
    // ------------------------------------------------------------
    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming call alerts"
            setSound(null, null) // 🔕 sound handled by MediaPlayer
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)
    }
}
