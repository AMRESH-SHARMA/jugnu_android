package com.example.app.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.theme.AppTheme
import com.example.app.ui.home.HomeScreen
import com.example.app.ui.home.HomeTab

// ⭐ FINAL PREVIEW ENTRY POINT
@AllDevicesPreview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        val navController = rememberNavController()

        HomeScreen(
            navController = navController,    // required param
            initialTab = HomeTab.LISTENERS,   // default value
            onContactClick = { _, _ -> },     // no-op lambda for preview
            modifier = Modifier
        )
    }
}