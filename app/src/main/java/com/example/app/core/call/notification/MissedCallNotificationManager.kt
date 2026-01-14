package com.example.app.core.call.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.app.MainActivity
import com.example.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissedCallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "missed_call_channel"
        private const val CHANNEL_NAME = "Missed Calls"

        // ✅ SINGLE notification ID (critical)
        private const val MISSED_CALL_NOTIFICATION_ID = 1001
    }

    // Track missed call count (process-local, acceptable for UX)
    private var missedCount = 0

    // ------------------------------------------------------------
    // PUBLIC API
    // ------------------------------------------------------------

    /**
     * Call this ONLY when:
     * - current user is callee
     * - call was not answered
     */
    fun showMissedCall(callId: String) {
        Log.d("RTM", "showMissedCall $callId")
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannelIfNeeded(manager)

        missedCount++

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0, // ✅ fixed requestCode (important when reusing notification)
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (missedCount == 1) {
            "Missed call"
        } else {
            "$missedCount missed calls"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lockphone)
            .setContentTitle("Missed call")
            .setContentText(contentText)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(contentIntent)
            .build()

        // ✅ UPDATE existing notification (do NOT stack)
        manager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
    }

    /**
     * Call this when user opens the app or clears missed calls
     */
    fun clearMissedCalls() {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        missedCount = 0
        manager.cancel(MISSED_CALL_NOTIFICATION_ID)
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
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Missed call notifications"
            setSound(null, null)
        }

        manager.createNotificationChannel(channel)
    }
}

/*
@Singleton
class MissedCallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "missed_call_channel"
        private const val CHANNEL_NAME = "Missed Calls"
    }

    // ------------------------------------------------------------
    // PUBLIC API
    // ------------------------------------------------------------

    /**
     * Call this when a call is missed (CANCELLED)
     * One missed call = one notification
     */
    fun showMissedCall(callId: String) {

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannelIfNeeded(manager)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(), // unique per missed call
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lockphone)
            .setContentTitle("Missed call")
            .setContentText("Tap to view")
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        manager.notify(callId.hashCode(), notification)
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
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Missed call notifications"
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }
}
 */
