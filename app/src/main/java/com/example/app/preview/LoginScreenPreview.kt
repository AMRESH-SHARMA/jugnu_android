package com.example.app.preview

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.app.feature.login.ui.LoginScreen
import com.example.app.feature.theme.AppTheme


@AllDevicesPreview
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            navController = rememberNavController()
        )
    }
}