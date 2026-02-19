package com.example.app.feature.navigation.ui

import Routes
import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.app.feature.login.ui.LoginScreen
import com.example.app.feature.login.ui.OtpVerificationScreen
import com.example.app.feature.login.ui.PermissionScreen
import com.example.app.feature.login.ui.ProfileSetupScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.Screen.Auth.LOGIN,
        route = Routes.Graph.AUTH
    ) {

        composable(Routes.Screen.Auth.LOGIN) {
            // Check if user should be redirected to permission or profile setup
            val context = LocalContext.current
            val isLoggedIn = com.example.app.core.session.SessionManager.userAccountId != 0L
            
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    // Check notification permission
                    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    
                    when {
                        // Missing notification permission
                        !hasNotificationPermission -> {
                            navController.navigate(Routes.Screen.Auth.PERMISSION) {
                                popUpTo(Routes.Screen.Auth.LOGIN) { inclusive = true }
                            }
                        }
                        
                        // Has permission but profile incomplete (Customer only)
                        com.example.app.core.session.SessionManager.userRole == com.example.app.core.preferences.user.domain.UserRole.CUSTOMER &&
                        !com.example.app.core.session.SessionManager.isProfileComplete -> {
                            navController.navigate(Routes.Screen.Auth.PROFILE_SETUP) {
                                popUpTo(Routes.Screen.Auth.LOGIN) { inclusive = true }
                            }
                        }
                        
                        // Everything complete, go to home
                        else -> {
                            val destination = if (com.example.app.core.session.SessionManager.userRole == com.example.app.core.preferences.user.domain.UserRole.LISTENER) {
                                Routes.Graph.LISTENER
                            } else {
                                Routes.Graph.HOME
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.Graph.AUTH) { inclusive = true }
                            }
                        }
                    }
                }
            }
            
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

        composable(Routes.Screen.Auth.PERMISSION) {
            PermissionScreen(navController = navController)
        }

        composable(Routes.Screen.Auth.PROFILE_SETUP) {
            ProfileSetupScreen(navController = navController)
        }
    }
}

