package com.example.app.core.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    fun provideGlobalExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            // 🔥 central logging place
            Log.e("APP:GLOBALERROR", "GlobalError Unhandled: ${throwable.message}", throwable)
            // Later: Crashlytics, Sentry, snackbar dispatcher, etc.
        }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        handler: CoroutineExceptionHandler
    ): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + handler)
}