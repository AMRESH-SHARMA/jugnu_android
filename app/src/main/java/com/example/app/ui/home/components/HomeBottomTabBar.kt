package com.example.app.ui.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.app.ui.home.HomeTab

@Composable
fun HomeBottomTabBar(
    selected: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selected == HomeTab.CALLS,
            onClick = { onTabSelected(HomeTab.CALLS) },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("Calls") }
        )
        NavigationBarItem(
            selected = selected == HomeTab.CHATS,
            onClick = { onTabSelected(HomeTab.CHATS) },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = selected == HomeTab.USER,
            onClick = { onTabSelected(HomeTab.USER) },
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "UserInfo") },
            label = { Text("User") }
        )
    }
}
