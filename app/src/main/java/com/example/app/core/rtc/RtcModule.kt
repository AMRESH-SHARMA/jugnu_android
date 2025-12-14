package com.example.app.core.rtc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RtcModule {

    @Binds
    @Singleton
    abstract fun bindRtcManager(
        impl: AgoraRtcManager
    ): RtcManager
}
