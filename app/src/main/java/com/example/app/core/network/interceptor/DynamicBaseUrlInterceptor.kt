package com.example.app.core.network.interceptor

import com.example.app.core.remoteconfig.RemoteConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentBase = RemoteConfig.apiBaseUrl.toHttpUrl()

        val newUrl = chain.request().url.newBuilder()
            .scheme(currentBase.scheme)
            .host(currentBase.host)
            .port(currentBase.port)
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}