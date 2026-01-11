package com.example.app.preview

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.app.feature.login.ui.OtpVerificationScreen
import com.example.app.feature.theme.AppTheme

@AllDevicesPreview
@Composable
fun OtpScreenPreview() {
    AppTheme {
        OtpVerificationScreen(
            navController = rememberNavController(),
            mobile = ""
        )
    }
}