package com.example.app.feature.call.ui

/*
import com.example.app.core.call.CallType
import com.example.app.core.permissions.Permission
import com.example.app.core.permissions.PermissionManager
import com.example.app.core.permissions.PermissionRequest

class CallPermissionCoordinator(
    private val permissionManager: PermissionManager
) {

    fun hasPermissions(callType: CallType): Boolean {
        return when (callType) {
            CallType.VOICE ->
                permissionManager.hasPermissions(
                    PermissionRequest(listOf(Permission.Microphone))
                )

            CallType.VIDEO ->
                permissionManager.hasPermissions(
                    PermissionRequest(
                        listOf(Permission.Microphone, Permission.Camera)
                    )
                )
        }
    }

    fun requestPermissions(callType: CallType, requestCode: Int) {
        val request = when (callType) {
            CallType.AUDIO ->
                PermissionRequest(listOf(Permission.Microphone))

            CallType.VIDEO ->
                PermissionRequest(
                    listOf(Permission.Microphone, Permission.Camera)
                )

            else -> {}
        }

        permissionManager.requestPermissions(request, requestCode)
    }
}


 */