package com.example.app.core.user

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val session: UserSession
) : ViewModel()

