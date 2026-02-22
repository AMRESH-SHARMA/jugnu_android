package com.example.app

import android.content.Intent
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
import android.util.Log
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
    
    @Inject
    lateinit var callRepository: com.example.app.feature.call.data.CallRepository

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
        handleIncomingIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        // Check if this is a return-to-call intent from notification
        val isFromNotification = intent?.getBooleanExtra("from_call_notification", false) == true
        
        if (isFromNotification) {
            // Broadcast event to navigate to call screen if there's an active call
            lifecycleScope.launch {
                val activeCall = com.example.app.core.call.CallStore.current()
                if (activeCall != null && activeCall.status in listOf(
                    com.example.app.feature.call.domain.CallStatus.OUTGOING_CONNECTING,
                    com.example.app.feature.call.domain.CallStatus.OUTGOING_RINGING,
                    com.example.app.feature.call.domain.CallStatus.CONNECTING,
                    com.example.app.feature.call.domain.CallStatus.CONNECTED
                )) {
                    // Emit a navigation event
                    CallEventBus.emit(CallEvent.NavigateToCall)
                }
            }
        }
    }

    private fun restorePendingIncomingCall() {
        lifecycleScope.launch {
            val pending = pendingCallStore.consume() ?: return@launch
            
            // 1️⃣ Client-side TTL check (fast path - avoid unnecessary API call)
            val age = System.currentTimeMillis() - pending.timestamp
            if (age > AppConstants.CALL_PENDING_STORE_TTL) {
                Log.w("RTM", "Pending call expired locally (${age}ms old), skipping")
                return@launch
            }
            
            // 2️⃣ Server verification (authoritative - prevents ghost calls)
            val result = callRepository.getCallState(pending.callId)
            when (result) {
                is com.example.app.core.network.ApiResult.Success -> {
                    val callState = result.data
                    if (!callState.isActive || callState.isExpired) {
                        Log.w("RTM", "Call not active on server: status=${callState.status}, isExpired=${callState.isExpired}")
                        return@launch
                    }
                    
                    // 3️⃣ Call is valid - emit event to show UI
                    Log.d("RTM", "Restoring valid pending call: ${pending.callId}")
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
                is com.example.app.core.network.ApiResult.Error -> {
                    Log.w("RTM", "Failed to verify call state: ${result.message}")
                    // Don't show call UI if we can't verify with server
                }
            }
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
                SessionManager.isProfileComplete = false
                
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

