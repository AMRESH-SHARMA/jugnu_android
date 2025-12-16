package com.example.app.core.audio
// MediaPlayer / SoundPool


import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .build()

    // ---------------------------------------------------------
    // PUBLIC API
    // ---------------------------------------------------------

    override fun play(type: AudioType) {
        when (type) {
            AudioType.IncomingCall ->
                playLoop(R.raw.incoming_call)

            AudioType.OutgoingCall ->
                playLoop(R.raw.outgoing_call)

            AudioType.MessageSent ->
                playOnce(R.raw.message_sent)

            AudioType.MessageReceived ->
                playOnce(R.raw.message_sent)
        }
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    // ---------------------------------------------------------
    // INTERNAL HELPERS
    // ---------------------------------------------------------

    private fun playLoop(resId: Int) {
        if (isPlaying()) return  // 🔥 prevent duplicate looping sounds

        stop()
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            start()
        }
    }

    private fun playOnce(resId: Int) {
        val soundId = soundPool.load(context, resId, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
}

