package com.example.app.feature.navigation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
//        authNavGraph(navController)
        homeNavGraph(navController)
        walletNavGraph(navController)
        callNavGraph(navController)
    }
}