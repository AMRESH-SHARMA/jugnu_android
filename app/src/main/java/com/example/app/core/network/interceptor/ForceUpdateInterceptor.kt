package com.example.app.core.network.interceptor

import com.example.app.root.ForceUpdateBus
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ForceUpdateInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val res = chain.proceed(chain.request())

        if (res.code == 426) {
            ForceUpdateBus.trigger()
        }

        return res
    }
}