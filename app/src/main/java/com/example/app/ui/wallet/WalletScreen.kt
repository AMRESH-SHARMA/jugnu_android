package com.example.app.ui.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.ui.home.components.HomeTopBar


@Composable
fun WalletScreen(
    navController: NavController,
    onBackClick: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            HomeTopBar(
                title = "Wallet",          // Set title for this screen
                showWalletSection = false, // Hide wallet button here (avoid looping)
//                balance = "₹1200",
                onWalletClick = { /* no-op */ }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
//                .statusBarsPadding()
//                .navigationBarsPadding()
        ) {

            Text(
                text = "Wallet Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This is an empty wallet screen placeholder.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
