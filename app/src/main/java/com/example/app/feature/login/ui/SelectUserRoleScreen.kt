package com.example.app.feature.login.ui

import Routes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.core.preferences.user.domain.UserRole

@Composable
fun SelectUserRoleScreen(
    navController: NavController,
    viewModel: SelectUserRoleViewModel = hiltViewModel()
) {
    val navigateToHome by viewModel.navigateToHome
    var accountId by remember { mutableStateOf("") }
    val savedRole by viewModel.savedRole

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {

            // ✅ Hide keyboard FIRST
            keyboardController?.hide()
            focusManager.clearFocus(force = true)

            when (savedRole) {

                UserRole.LISTENER -> {
                    navController.navigate(Routes.Graph.LISTENER) {
                        popUpTo(Routes.Screen.SelectUserRole.ROOT) { inclusive = true }
                    }
                }

                UserRole.CUSTOMER -> {
                    navController.navigate(Routes.Graph.HOME) {
                        popUpTo(Routes.Screen.SelectUserRole.ROOT) { inclusive = true }
                    }
                }

                else -> Unit
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                onClick = { viewModel.save(accountId.toLongOrNull() ?: 1, UserRole.CUSTOMER) }
            ) {
                Text("Customer")
            }

            Button(
                enabled = accountId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                onClick = { viewModel.save(accountId.toLongOrNull() ?: 2, UserRole.LISTENER) }
            ) {
                Text("Listener")
            }
        }
    }
}