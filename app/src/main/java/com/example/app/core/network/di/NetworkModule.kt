package com.example.app.core.network.di

import com.example.app.BuildConfig
import com.example.app.core.network.appconfig.AppConfigApi
import com.example.app.core.network.appconfig.AppConfigRepository
import com.example.app.core.network.data.ApiRepository
import com.example.app.core.network.data.CallNotificationApi
import com.example.app.core.network.data.RtmAuthApi
import com.example.app.core.network.interceptor.DynamicBaseUrlInterceptor
import com.example.app.core.network.interceptor.ForceUpdateInterceptor
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.feature.call.data.CallApi
import com.example.app.feature.listenerDashboard.data.ListenerDashBoardApi
import com.example.app.feature.listeners.data.ListenerApi
import com.example.app.feature.login.data.AuthApi
import com.example.app.feature.user.data.UserApi
import com.example.app.feature.wallet.data.PaymentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // -----------------------------
    // Interceptor: adds version header globally before every request goes out.
    // -----------------------------
    @Provides
    @Singleton
    fun provideVersionInterceptor(): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request()
                .newBuilder()
                .addHeader(
                    "X-App-Version",
                    BuildConfig.VERSION_CODE.toString()
                )
                .build()

            chain.proceed(newRequest)
        }

    // -----------------------------
    // OkHttp client with interceptor
    // -----------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(
        versionInterceptor: Interceptor,
        forceUpdateInterceptor: ForceUpdateInterceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(versionInterceptor)
            .addInterceptor(forceUpdateInterceptor)
            .addInterceptor(dynamicBaseUrlInterceptor)
            .build()

    // -----------------------------
    // Retrofit using this client
    // -----------------------------
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(RemoteConfig.DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

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

    // -----------------------------
    // Payment API
    // -----------------------------
    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)

    // -----------------------------
    // Listener Dashboard
    // -----------------------------
    @Provides
    @Singleton
    fun provideListenerDashboardApi(
        retrofit: Retrofit
    ): ListenerDashBoardApi {
        return retrofit.create(ListenerDashBoardApi::class.java)
    }

    // -----------------------------
    // Force Update
    // -----------------------------
    @Provides
    @Singleton
    fun provideAppConfigApi(
        retrofit: Retrofit
    ): AppConfigApi =
        retrofit.create(AppConfigApi::class.java)

    @Provides
    @Singleton
    fun provideAppConfigRepository(
        api: AppConfigApi
    ): AppConfigRepository = AppConfigRepository(api)

}
