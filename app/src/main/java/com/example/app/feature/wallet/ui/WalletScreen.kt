package com.example.app.feature.wallet.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.core.user.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    onBackClick: (() -> Boolean)?,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPrefs.collectAsState()
    val role = prefs.second

    // ⭐ TopAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ⭐ Animated Balance Card
            WalletBalanceCard(
                balance = 12450.75,
                currency = "USD"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ⭐ Quick Actions Row
            QuickActionButtons(role)

            Spacer(modifier = Modifier.height(24.dp))

            // ⭐ Transaction History
            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            TransactionItem("Coffee Shop", "-$5.40", "Today • 09:45 AM")
            TransactionItem("Salary", "+$2,500.00", "Yesterday • 06:00 AM")
            TransactionItem("Gym Membership", "-$40.00", "Jun 2 • 09:00 PM")
            TransactionItem("Electricity Bill", "-$120.00", "Jun 1 • 02:12 PM")
        }
    }
}


// -------------------------------------------
// ⭐ Balance Card With Animation
// -------------------------------------------
@Composable
fun WalletBalanceCard(balance: Double, currency: String) {

    val animatedValue by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "balanceAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )

            Text(
                text = "$currency ${"%,.2f".format(animatedValue)}",
                style = MaterialTheme.typography.headlineLarge.copy(color = Color.White)
            )
        }
    }
}


// -------------------------------------------
// ⭐ Quick Action Buttons (Send / Receive / Add Money)
// -------------------------------------------
@Composable
fun QuickActionButtons(role: UserRole) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
//        QuickAction("Send", Icons.Default.ArrowUpward)
//        QuickAction("Receive", Icons.Default.ArrowDownward)
//        QuickAction("Add Money", Icons.Default.AddCircle)
        when (role) {

            UserRole.LISTENER -> {
                QuickAction("Withdraw Money", Icons.Default.ArrowDownward)
            }

            UserRole.CUSTOMER -> {
                QuickAction("Add Money", Icons.Default.AddCircle)
            }
        }
    }
}

@Composable
fun QuickAction(title: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// -------------------------------------------
// ⭐ Transaction Item
// -------------------------------------------
@Composable
fun TransactionItem(title: String, amount: String, time: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {

        Surface(
            shape = CircleShape,
            tonalElevation = 2.dp,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(title.take(1), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Text(
            amount,
            style = MaterialTheme.typography.bodyLarge,
            color = if (amount.startsWith("-")) Color.Red else Color(0xFF2ECC71)
        )
    }
}