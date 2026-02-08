package com.example.app.feature.login.ui

import Routes
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.feature.components.HeadingTextComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController
) {
    val viewModel: ProfileSetupViewModel = hiltViewModel()
    val context = LocalContext.current
    
    var nickname by remember { mutableStateOf("Anonymous") }
    var selectedGender by remember { mutableStateOf("MALE") }
    
    val setupState by viewModel.setupState.collectAsState()
    
    // Handle setup completion
    LaunchedEffect(setupState) {
        when (setupState) {
            is ProfileSetupUiState.Success -> {
                Toast.makeText(context, "Profile setup complete!", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.Graph.HOME) {
                    popUpTo(Routes.Graph.AUTH) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is ProfileSetupUiState.Error -> {
                val msg = (setupState as ProfileSetupUiState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            HeadingTextComponent("Complete Your Profile")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Help us personalize your experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Nickname Field
            Text(
                text = "Nickname",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your nickname") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Interested In Section
            Text(
                text = "Interested In",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenderOption(
                    text = "Male",
                    selected = selectedGender == "MALE",
                    onClick = { selectedGender = "MALE" }
                )
                
                GenderOption(
                    text = "Female",
                    selected = selectedGender == "FEMALE",
                    onClick = { selectedGender = "FEMALE" }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue Button
            Button(
                onClick = {
                    if (nickname.isBlank()) {
                        Toast.makeText(context, "Please enter a nickname", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.setupProfile(nickname.trim(), selectedGender)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                enabled = setupState !is ProfileSetupUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (setupState is ProfileSetupUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else 
            MaterialTheme.colorScheme.surface,
        border = if (selected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
