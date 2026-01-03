package com.example.app.feature.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.feature.wallet.domain.AmountFlowType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterAmountScreen(
    navController: NavController,
    flowType: AmountFlowType,
    viewModel: EnterAmountViewModel = hiltViewModel()
) {
    val amount by viewModel.amount.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val canContinue by viewModel.canContinue.collectAsState()
    val balance by viewModel.currentBalance.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (flowType == AmountFlowType.ADD)
                            "Add Money"
                        else
                            "Withdraw Money"
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = MaterialTheme.colorScheme.onTertiary,

                            )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🪙 %,d".format(balance),
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                    }
                }

            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            EnterAmountContent(
                flowType = flowType,
                amount = amount,
                error = error,
                onAmountChange = viewModel::onAmountChange,
                onQuickAmountClick = viewModel::onQuickAmountClick
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue && !loading,
                onClick = {
                    viewModel.onContinue(flowType)
                }
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue", color = MaterialTheme.colorScheme.onTertiary)
                }
            }
        }
    }
}

@Composable
fun EnterAmountContent(
    flowType: AmountFlowType,
    amount: String,
    error: String?,
    onAmountChange: (String) -> Unit,
    onQuickAmountClick: (Int) -> Unit
) {

    Column {

        Text(
            text = "Enter Amount",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            leadingIcon = {
                Text("₹", style = MaterialTheme.typography.titleLarge)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        QuickAmountRow(
            onQuickAmountClick = onQuickAmountClick
        )
    }
}

@Composable
fun QuickAmountRow(
    onQuickAmountClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf(100, 500, 1000).forEach { amount ->
            AssistChip(
                onClick = { onQuickAmountClick(amount) },
                label = { Text("₹$amount") }
            )
        }
    }
}
