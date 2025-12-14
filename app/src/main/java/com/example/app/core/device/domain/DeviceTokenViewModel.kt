package com.example.app.core.device.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.core.device.data.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceTokenViewModel @Inject constructor(
    private val repo: DeviceRepository
) : ViewModel() {

    fun sendToken(userId: Long, token: String) {
        viewModelScope.launch {
            try {
                repo.sendDeviceToken(userId, token)
            } catch (e: Exception) {
                // ignore errors
            }
        }
    }
}