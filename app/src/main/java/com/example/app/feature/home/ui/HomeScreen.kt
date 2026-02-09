package com.example.app.feature.home.ui

import Routes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.feature.home.ui.components.HomeBottomTabBar
import com.example.app.feature.home.ui.components.HomeTopBar
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.ui.list.ListenerListScreen
import com.example.app.feature.user.ui.UserSettingScreen

enum class HomeTab { LISTENERS, RECENTS, USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    initialTab: HomeTab = HomeTab.LISTENERS,
    onListenerClick: (ListenerModel) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val balance by viewModel.balance.collectAsState()
    
    // Get the saved tab from navigation back stack entry
    val navBackStackEntry = navController.currentBackStackEntry
    val savedTab = navBackStackEntry?.savedStateHandle?.get<String>("selected_tab")
    
    var currentTab by remember { 
        mutableStateOf(
            savedTab?.let { 
                try { HomeTab.valueOf(it) } catch (e: Exception) { initialTab }
            } ?: initialTab
        ) 
    }
    
    // Save tab state when it changes
    androidx.compose.runtime.LaunchedEffect(currentTab) {
        navBackStackEntry?.savedStateHandle?.set("selected_tab", currentTab.name)
    }


    Scaffold(
        bottomBar = {
            HomeBottomTabBar(
                selected = currentTab,
                onTabSelected = { selected -> currentTab = selected },
                elevation = 0.dp,
                modifier = Modifier
                    .height(120.dp)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Show header only when NOT on Settings tab
            if (currentTab != HomeTab.USER) {
                CustomerHeaderSection(
                    username = "Anonymous",
                    balance = balance,
                    onWalletClick = {
                        navController.navigate(Routes.Graph.WALLET) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // Scrollable Main Content
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (currentTab == HomeTab.USER) 0.dp else 12.dp),
                shape = if (currentTab == HomeTab.USER) 
                    RoundedCornerShape(0.dp) 
                else 
                    RoundedCornerShape(8.dp),
                tonalElevation = if (currentTab == HomeTab.USER) 0.dp else 1.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {   // NEW: ensures LazyColumn gets full height
                    when (currentTab) {
                        HomeTab.RECENTS -> com.example.app.feature.recents.ui.RecentsScreen(
                            navController = navController
                        )

                        HomeTab.LISTENERS -> ListenerListScreen(
                            modifier = Modifier.fillMaxSize(),
                            navController = navController,
                            onOpenListener = { listener ->
                                onListenerClick(listener)
                            }
                        )

                        HomeTab.USER -> UserSettingScreen(
                            onWalletClick = { 
                                navController.navigate(Routes.Graph.WALLET) {
                                    launchSingleTop = true
                                }
                            },
                            onUsageClick = {
                                navController.navigate(Routes.Screen.Usage.STATISTICS) {
                                    launchSingleTop = true
                                }
                            },
                            onLogout = {
                                navController.navigate(Routes.Graph.AUTH) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------
   CUSTOMER HEADER SECTION
--------------------------------------------------- */
@Composable
fun CustomerHeaderSection(
    username: String,
    balance: Long,
    onWalletClick: () -> Unit
) {
    val currentHour = java.time.LocalTime.now().hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Wallet Balance - Premium Design
            Card(
                modifier = Modifier.clickable(onClick = onWalletClick),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.15f) // Gold tint
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Coin icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFA500)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪙",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 16.sp
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "%,d".format(balance),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp
                        )
                    }
                }
            }

        }
    }
}

/** Sample contact data */
data class Contact(val id: String, val name: String, val avatarColorHex: Long)

private fun sampleContacts(): List<Contact> {
    return List(12) { index ->
        Contact(id = "c$index", name = "Contact $index", avatarColorHex = 0xFFCCCCCC)
    }
}