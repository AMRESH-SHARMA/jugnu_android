package com.example.app.core.audio

// what to play
sealed class AudioType {
    object IncomingCall : AudioType()
    object OutgoingCall : AudioType()
    object MessageSent : AudioType()
    object MessageReceived : AudioType()

    object Beep : AudioType()
}
