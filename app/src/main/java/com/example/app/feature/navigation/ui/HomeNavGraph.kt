package com.example.app.feature.navigation.ui

import Routes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import com.example.app.core.remoteconfig.RemoteConfig
import com.example.app.feature.home.ui.HomeScreen
import com.example.app.feature.home.ui.HomeTab
import com.example.app.feature.home.ui.HomeViewModel
import com.example.app.feature.home.ui.components.HomeOfferDialog

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeNavGraph(navController: NavController) {
    navigation(
        route = Routes.Graph.HOME,
        startDestination = Routes.Screen.Home.ROOT
    ) {
        composable(Routes.Screen.Home.ROOT) {

            // 🔐 Offer logic scoped to HOME entry
            val viewModel: HomeViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                viewModel.showOfferEvent.collect {
                    navController.navigate(Routes.Screen.Home.OFFER_MODAL)
                }
            }

            HomeScreen(
                navController,
                onContactClick = { tab, listener ->
                    if (tab == HomeTab.CHATS) {
                        navController.openChat(listener)
                    }
                }

            )
        }


        // ---------------------------------------------------------
        // 🎁 HOME-ONLY OFFER MODAL
        // ---------------------------------------------------------
        dialog(
            route = Routes.Screen.Home.OFFER_MODAL,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val viewModel: HomeViewModel = hiltViewModel()
            val offer = RemoteConfig.getOffer() ?: return@dialog
            HomeOfferDialog(
                title = offer.title,
                body = offer.body,
                onDismiss = {
                    viewModel.markOfferShown()
                    navController.popBackStack()
                }
            )
        }

    }

    chatNavGraph(
        navController = navController,
        onBack = { navController.popBackStack() }
    )
    walletNavGraph(
        navController = navController,
    )
}