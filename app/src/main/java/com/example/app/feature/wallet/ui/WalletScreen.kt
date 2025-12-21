package com.example.app.feature.wallet.ui

import Routes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.core.preferences.user.domain.UserRole
import com.example.app.feature.wallet.domain.AmountFlowType
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    onBackClick: (() -> Boolean)?
) {
    val walletVM: WalletViewModel = hiltViewModel()
    val historyVM: WalletHistoryViewModel = hiltViewModel()

    val role = walletVM.role
    val balance by walletVM.balance.collectAsState()
    val items by historyVM.items.collectAsState()
    val loading by historyVM.loading.collectAsState()
    val listState = rememberLazyListState()
    // ⭐ TopAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    LaunchedEffect(Unit) {
        walletVM.refreshBalance()
        historyVM.loadNextPage()
    }
    // 🔥 Pagination trigger
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= items.size - 3) {
                    historyVM.loadNextPage()
                }
            }
    }

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
                balance = balance.toDouble(),
                currency = "₹"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ⭐ Quick Actions Row
            QuickActionButtons(
                role = role,
                navController = navController
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // ⭐ Transaction History
            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (items.isEmpty()) {
                Text(
                    "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                items.forEach { txn ->
                    TransactionItem(
                        title = txn.reason,
                        amount = formatAmount(txn.amount),
                        time = formatTime(txn.time)
                    )
                }
            }

        }
    }
}

private fun formatAmount(amount: Long): String {
    return if (amount < 0) {
        "-₹${kotlin.math.abs(amount)}"
    } else {
        "+₹$amount"
    }
}

private fun formatTime(timestamp: Instant): String {
    // TEMP: replace later with proper formatter
    return "Just now"
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
            .height(120.dp),
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
fun QuickActionButtons(
    role: UserRole,
    navController: NavController
) {
    when (role) {

        UserRole.CUSTOMER -> {
            HorizontalActionButton(
                title = "Add Money",
                icon = Icons.Filled.AddCircle,
                onClick = {
                    navController.navigate(
                        enterAmount(
                            AmountFlowType.ADD.name
                        )
                    )
                }
            )
        }

        UserRole.LISTENER -> {
            HorizontalActionButton(
                title = "Withdraw Money",
                icon = Icons.Filled.ArrowDownward,
                onClick = {
                    navController.navigate(
                        enterAmount(
                            AmountFlowType.WITHDRAW.name
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun HorizontalActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ⬅️ Left text
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.weight(1f))

            // ➡️ Right icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
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

fun enterAmount(type: String): String {
    return "${Routes.Screen.Wallet.ENTER_AMOUNT}/$type"
}