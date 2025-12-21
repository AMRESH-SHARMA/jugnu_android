package com.example.app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // -------------------------
            // WebSocket Ping/Pong frames are NOT delivered as messages.
            // pingInterval send ping and expects pong within passed pingInterval value
            // -------------------------
            .pingInterval(30, TimeUnit.SECONDS)
            // Optional: timeouts (safe defaults)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // WS must not timeout
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
}
