package com.example.app.core.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.example.app.MainActivity
import com.example.app.R

class CallForegroundService : Service() {

    companion object {
        const val EXTRA_CALL_TYPE = "call_type"
        const val CALL_TYPE_VOICE = "voice"
        const val CALL_TYPE_VIDEO = "video"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callType = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: CALL_TYPE_VOICE
        startAsForeground(callType)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startAsForeground(callType: String) {

        val channelId = "call_channel"
        val channelName = "Ongoing Calls"

        // ---- Notification channel ----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps calls active"
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                }

                manager.createNotificationChannel(channel)
            }
        }

        // Person (who the call is "with") — can just be generic text
        val person = Person.Builder()
            .setName("In a call")
            .build()

        // Tapping the notification returns user to the call UI
        val returnIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("In a call")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // ---- Native call UI ----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    person,
                    returnIntent
                )
            )
        } else {
            // Fallback on Android 7–11
            builder.setContentText("Tap to return")
        }

        val notification = builder.build()

        // ---- Start as foreground service with appropriate type ----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val serviceType = if (callType == CALL_TYPE_VIDEO) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            
            startForeground(1001, notification, serviceType)
        } else {
            startForeground(1001, notification)
        }
    }
}
