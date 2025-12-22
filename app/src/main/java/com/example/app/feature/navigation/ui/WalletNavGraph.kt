package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.listeners.ui.ListenerDashboardScreen
import com.example.app.feature.wallet.domain.AmountFlowType
import com.example.app.feature.wallet.ui.EnterAmountScreen

fun NavGraphBuilder.walletNavGraph(navController: NavController) {

    navigation(
        startDestination = Routes.Screen.Wallet.ROOT,
        route = Routes.Graph.WALLET
    ) {
        composable(Routes.Screen.Wallet.ROOT) {
            ListenerDashboardScreen(
            )
//            WalletScreen(
//                navController,
//                onBackClick = { navController.popBackStack() }
//            )
        }

        composable(
            route = "${Routes.Screen.Wallet.ENTER_AMOUNT}/{type}"
        ) { backStackEntry ->

            val type = backStackEntry.arguments
                ?.getString("type")
                ?.uppercase()

            val flowType = AmountFlowType.valueOf(type!!)

            EnterAmountScreen(
                navController = navController,
                flowType = flowType
            )
        }
    }
}
