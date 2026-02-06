package com.example.app

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.PendingCallStore
import com.example.app.core.session.SessionManager
import com.example.app.feature.theme.AppTheme
import com.example.app.root.AppRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var pendingCallStore: PendingCallStore

    override fun onCreate(savedInstanceState: Bundle?) {
        val transparentSystemBarStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )
        enableEdgeToEdge(
            navigationBarStyle = transparentSystemBarStyle
        )

        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = false
        }

        restorePendingIncomingCall()

        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppRoot()   // 👈 root that decides update vs nav graph
                }
            }
        }
    }

    private fun restorePendingIncomingCall() {
        lifecycleScope.launch {
            val pending = pendingCallStore.consume() ?: return@launch

            CallEventBus.emit(
                CallEvent.Incoming(
                    callId = pending.callId,
                    callType = pending.callType,
                    callerAccountId = pending.callerAccountId,
                    calleeAccountId = SessionManager.userAccountId,
                    channel = null
                )
            )
        }
    }
}



