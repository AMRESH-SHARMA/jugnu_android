package com.example.app.feature.navigation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.app.feature.login.ui.SelectUserRoleScreen

object SelectUserRoleRoutes{

    const val ROOT = "select_user_role_root"

}

fun NavGraphBuilder.selectUserRoleNavGraph(navController: NavController) {
    navigation(
        startDestination = SelectUserRoleRoutes.ROOT,
        route = Routes.SELECT_USER_ROLE
    ) {
        composable(SelectUserRoleRoutes.ROOT) {
            SelectUserRoleScreen(
                navController = navController
            )
        }
    }
}