package com.example.app.core.device.di

import com.example.app.core.device.data.DeviceApi
import com.example.app.core.device.data.DeviceRepository
import com.example.app.core.device.domain.SendDeviceTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi =
        retrofit.create(DeviceApi::class.java)

    @Provides
    @Singleton
    fun provideDeviceRepository(api: DeviceApi) =
        DeviceRepository(api)

    @Provides
    @Singleton
    fun provideSendDeviceTokenUseCase(repo: DeviceRepository) =
        SendDeviceTokenUseCase(repo)
}
