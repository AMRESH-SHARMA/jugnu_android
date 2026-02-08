package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.wallet.domain.AmountFlowType
import com.example.app.feature.wallet.ui.EnterAmountScreen
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

        composable(
            route = Routes.Screen.Wallet.ENTER_AMOUNT
        ) { backStackEntry ->

            val flowTypeString = backStackEntry.arguments
                ?.getString("flowType")
                ?.uppercase()

            val flowType = AmountFlowType.valueOf(flowTypeString!!)

            EnterAmountScreen(
                navController = navController,
                flowType = flowType
            )
        }
    }
}
