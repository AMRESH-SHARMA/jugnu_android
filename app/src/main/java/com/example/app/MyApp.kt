package com.example.app

import android.app.Application
import com.example.app.core.user.TokenManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        tokenManager.start()
    }
}
