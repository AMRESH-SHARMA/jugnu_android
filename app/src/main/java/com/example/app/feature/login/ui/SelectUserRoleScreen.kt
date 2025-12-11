package com.example.app.feature.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.core.user.domain.model.UserRole
import com.example.app.feature.navigation.ui.Routes
import com.example.app.feature.navigation.ui.SelectUserRoleRoutes

@Composable
fun SelectUserRoleScreen(
    navController: NavController,
    viewModel: SelectUserRoleViewModel = hiltViewModel()
) {
    var accountId by remember { mutableStateOf("") }

    val navigateToHome by viewModel.navigateToHome
    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate(Routes.HOME) {
                popUpTo(SelectUserRoleRoutes.ROOT) { inclusive = true }
            }
            viewModel.resetNavigationFlag()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Enter Account ID", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = accountId,
            onValueChange = { accountId = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter account id") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Select User Role", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                enabled = accountId.isNotBlank(),
                onClick = { viewModel.save(accountId, UserRole.CUSTOMER) }
            ) {
                Text("Customer")
            }

            Button(
                enabled = accountId.isNotBlank(),
                onClick = { viewModel.save(accountId, UserRole.LISTENER) }
            ) {
                Text("Listener")
            }
        }
    }
}