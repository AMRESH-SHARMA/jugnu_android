package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.PendingCallStore
import com.example.app.core.session.SessionManager
import com.example.app.feature.theme.AppTheme
import com.example.app.root.AppConfigViewModel
import com.example.app.root.AppRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var pendingCallStore: PendingCallStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Hide navigation bar immediately for splash screen
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = false
        }

        restorePendingIncomingCall()
        observeSessionExpiry()

        setContent {
            AppTheme {
                // Observe app state to show navigation bar when splash finishes
                val vm: AppConfigViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val state by vm.appConfig.collectAsState()
                
                // Show navigation bar only when splash finishes
                LaunchedEffect(state.isLoading) {
                    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                    
                    if (!state.isLoading) {
                        // Splash finished - show navigation bar
                        insetsController.show(WindowInsetsCompat.Type.navigationBars())
                        // Set navigation bar color to match WhatsApp dark mode background
                        window.navigationBarColor = Color(0xFF0B141B).toArgb()
                    }
                }
                
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        AppRoot()   // 👈 root that decides update vs nav graph
                    }
                }
                
                // Global Snackbar - Outside Scaffold for highest z-index, even above dialogs
                com.example.app.core.ui.GlobalSnackbar()
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
    
    private fun observeSessionExpiry() {
        lifecycleScope.launch {
            com.example.app.core.network.SessionExpiryHandler.sessionExpiredEvent.collect {
                // Clear session
                SessionManager.userAccountId = 0L
                SessionManager.userRole = null
                SessionManager.sessionId = ""
                SessionManager.fcmToken = null
                
                // Show dialog
                com.example.app.core.ui.SnackbarManager.showError(
                    "Session expired. Please login again"
                )
                
                // Navigate to login will happen automatically via AppNavGraph
                // when it detects SessionManager.userAccountId == 0L
            }
        }
    }
}

