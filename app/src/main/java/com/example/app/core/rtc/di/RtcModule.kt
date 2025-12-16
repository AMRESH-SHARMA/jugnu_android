package com.example.app.core.rtc.di

import com.example.app.core.rtc.DefaultRtcManagerFactory
import com.example.app.core.rtc.RtcManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RTCModule {

    @Provides
    fun provideRtcManagerFactory(
        factory: DefaultRtcManagerFactory
    ): RtcManagerFactory = factory
}

