package com.example.app.feature.session

import androidx.lifecycle.ViewModel
import com.example.app.core.session.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val session: UserSession
) : ViewModel()