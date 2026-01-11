package com.example.app.feature.navigation.ui

import Routes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.app.feature.login.ui.LoginScreen
import com.example.app.feature.login.ui.OtpVerificationScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.Screen.Auth.LOGIN,
        route = Routes.Graph.AUTH
    ) {

        composable(Routes.Screen.Auth.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(
            route = Routes.Screen.Auth.OTP,
            arguments = listOf(
                navArgument("mobile") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mobile =
                backStackEntry.arguments?.getString("mobile") ?: ""

            OtpVerificationScreen(
                navController = navController,
                mobile = mobile
            )
        }
    }
}

