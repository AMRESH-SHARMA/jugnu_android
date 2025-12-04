package com.example.app.preview

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.theme.AppTheme
import com.example.app.ui.wallet.WalletScreen

@AllDevicesPreview
@Composable
fun WalletScreenPreview() {
    val navController = rememberNavController()
    AppTheme {
        WalletScreen(
            navController = navController,
            onBackClick = null,
        )
    }
}
