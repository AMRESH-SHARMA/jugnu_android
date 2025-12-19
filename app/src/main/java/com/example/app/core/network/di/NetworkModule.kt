package com.example.app.core.network.di

import com.example.app.core.network.data.ApiRepository
import com.example.app.core.network.data.CallNotificationApi
import com.example.app.core.network.data.RtmAuthApi
import com.example.app.feature.call.data.CallApi
import com.example.app.feature.listeners.data.ListenerApi
import com.example.app.feature.user.data.UserApi
import com.example.app.utils.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("${AppConstants.BASE_URL}api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideListenerApi(retrofit: Retrofit): ListenerApi =
        retrofit.create(ListenerApi::class.java)

    @Provides
    @Singleton
    fun provideCallApi(retrofit: Retrofit): CallApi =
        retrofit.create(CallApi::class.java)

    // -----------------------------
    // RTM Token Auth API
    // -----------------------------
    @Provides
    @Singleton
    fun provideRtmAuthApi(retrofit: Retrofit): RtmAuthApi =
        retrofit.create(RtmAuthApi::class.java)

    // -----------------------------
    // Call FCM Fallback API
    // -----------------------------
    @Provides
    @Singleton
    fun provideCallNotificationApi(retrofit: Retrofit): CallNotificationApi =
        retrofit.create(CallNotificationApi::class.java)

    // -----------------------------
    // Combined Repository
    // -----------------------------
    @Provides
    @Singleton
    fun provideApiRepository(
        authApi: RtmAuthApi,
        callNotificationApi: CallNotificationApi
    ): ApiRepository =
        ApiRepository(authApi, callNotificationApi)

    @Provides
    @Singleton
    fun provideUserApi(
        retrofit: Retrofit
    ): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}
