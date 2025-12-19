package com.example.app.core.util

/**
 * TODO
 * App crash ≠ network loss ≠ background kill
 * Network Monitor is fast failure
 * Heart beat is silent failure.
 * */
/*
class NetworkMonitor(
    context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            UserStatusManager.onAppForeground()
        }

        override fun onLost(network: Network) {
            UserStatusManager.onNetworkLost()
        }
    }

    fun register() {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}

*/