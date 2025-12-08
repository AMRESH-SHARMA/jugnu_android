package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.wallet.ui.WalletScreen

object WalletRoutes {
    const val ROOT = "wallet_root"
}

fun NavGraphBuilder.walletNavGraph(navController: NavController) {

    navigation(
        startDestination = WalletRoutes.ROOT,
        route = Routes.WALLET
    ) {
        composable(WalletRoutes.ROOT) {
            WalletScreen(
                navController,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
