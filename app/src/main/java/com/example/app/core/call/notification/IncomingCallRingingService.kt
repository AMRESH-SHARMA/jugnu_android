package com.example.app.core.call.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IncomingCallRingingService : Service() {

    @Inject
    lateinit var notificationManager: IncomingCallNotificationManager
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        fun stop(context: Context) {
            context.stopService(
                Intent(context, IncomingCallRingingService::class.java)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.incoming_call).apply {
            isLooping = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val callId = intent?.getStringExtra("callId") ?: return START_NOT_STICKY
        val callType = intent.getStringExtra("callType") ?: "Incoming call"

        startForeground(
            2001,
            notificationManager.buildForegroundNotification(callId, callType)
        )

        mediaPlayer?.start()

        // ⏱ Auto-stop after 30s (missed call)
        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 30_000)

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
