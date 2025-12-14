package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.login.ui.SelectUserRoleScreen

fun NavGraphBuilder.selectUserRoleNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.Screen.SelectUserRole.ROOT,
        route = Routes.Graph.SELECT_USER_ROLE
    ) {
        composable(Routes.Screen.SelectUserRole.ROOT) {
            SelectUserRoleScreen(
                navController = navController
            )
        }
    }
}