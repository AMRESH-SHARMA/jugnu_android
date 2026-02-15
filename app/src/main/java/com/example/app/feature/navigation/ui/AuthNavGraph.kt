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
import com.example.app.feature.login.ui.ProfileSetupScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    // Determine auth graph start destination based on profile completion
    val authStartDestination = if (com.example.app.core.session.SessionManager.userAccountId != 0L && 
                                     !com.example.app.core.session.SessionManager.isProfileComplete) {
        Routes.Screen.Auth.PROFILE_SETUP
    } else {
        Routes.Screen.Auth.LOGIN
    }
    
    navigation(
        startDestination = authStartDestination,
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

        composable(Routes.Screen.Auth.PROFILE_SETUP) {
            ProfileSetupScreen(navController = navController)
        }
    }
}

