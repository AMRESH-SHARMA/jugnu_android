package com.example.app.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import com.example.app.core.session.SessionManager
import javax.inject.Inject


class SessionInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder = request.newBuilder()

        if (SessionManager.sessionId.isNotEmpty()) {
            builder.addHeader("X-Session-ID", SessionManager.sessionId)
        }

        return chain.proceed(builder.build())
    }
}
