package com.example.app.core.websocket

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PresenceViewModel @Inject constructor(
    val remotePresenceStore: RemotePresenceStore
) : ViewModel()
