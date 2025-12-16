package com.example.app.core.audio

// abstraction
interface AudioPlayer {
    fun play(type: AudioType)
    fun stop()
    
    fun isPlaying(): Boolean
}
