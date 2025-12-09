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
import com.example.app.feature.navigation.ui.AppNavGraph
import com.example.app.feature.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

        // 🔥 read FCM extras here
        val route = intent.getStringExtra("route")
        val callerId = intent.getLongExtra("callerId", 0L)
        val calleeId = intent.getLongExtra("calleeId", 0L)
        val callId = intent.getStringExtra("callId")

        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // 🔥 pass to AppNavGraph
                    AppNavGraph(
                        route = route,
                        callerId = callerId,
                        calleeId = calleeId,
                        callId = callId,
                    )
                }
            }
        }
    }
}