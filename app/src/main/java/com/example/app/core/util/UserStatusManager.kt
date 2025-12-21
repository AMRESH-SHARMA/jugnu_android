package com.example.app.core.util

/*
TODO
object UserStatusManager {

    private var currentStatus: UserStatus = UserStatus.OFFLINE
    private var appearOffline: Boolean = false

    fun setAppearOffline(enabled: Boolean) {
        appearOffline = enabled
        updateStatus(
            if (enabled) UserStatus.OFFLINE else UserStatus.ONLINE
        )
    }

    fun onAppForeground() {
        if (!appearOffline && currentStatus != UserStatus.BUSY) {
            updateStatus(UserStatus.ONLINE)
        }
    }

    fun onAppBackground() {
        if (currentStatus != UserStatus.BUSY) {
            updateStatus(UserStatus.OFFLINE)
        }
    }

    fun onCallStarted() {
        updateStatus(UserStatus.BUSY)
    }

    fun onCallEnded() {
        if (appearOffline) {
            updateStatus(UserStatus.OFFLINE)
        } else {
            updateStatus(UserStatus.ONLINE)
        }
    }

    fun onNetworkLost() {
        updateStatus(UserStatus.OFFLINE)
    }

    fun getStatus(): UserStatus = currentStatus

    private fun updateStatus(newStatus: UserStatus) {
        if (currentStatus == newStatus) return

        currentStatus = newStatus
        sendStatusToServer(newStatus)
    }

    private fun sendStatusToServer(status: UserStatus) {
        // TODO: API / Socket / Firebase call
        // Example:
        // api.updateStatus(status.name)
    }
}

*/