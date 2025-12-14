package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.wallet.ui.WalletScreen

fun NavGraphBuilder.walletNavGraph(navController: NavController) {

    navigation(
        startDestination = Routes.Screen.Wallet.ROOT,
        route = Routes.Graph.WALLET
    ) {
        composable(Routes.Screen.Wallet.ROOT) {
            WalletScreen(
                navController,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
