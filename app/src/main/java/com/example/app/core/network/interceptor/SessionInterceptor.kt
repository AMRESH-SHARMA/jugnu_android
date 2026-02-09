package com.example.app.core.network.interceptor

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import com.example.app.core.session.SessionManager
import javax.inject.Inject


class SessionInterceptor @Inject constructor() : Interceptor {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder = request.newBuilder()

        if (SessionManager.sessionId.isNotEmpty()) {
            builder.addHeader("X-Session-ID", SessionManager.sessionId)
        }

        val response = chain.proceed(builder.build())
        
        // Check for session expiry
        if (response.code == 401 || response.code == 403) {
            Log.w("SessionInterceptor", "Session expired (${response.code})")
            scope.launch {
                com.example.app.core.network.SessionExpiryHandler.notifySessionExpired()
            }
        }
        
        return response
    }
}
