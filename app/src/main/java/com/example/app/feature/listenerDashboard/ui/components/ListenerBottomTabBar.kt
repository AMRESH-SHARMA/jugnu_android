package com.example.app.feature.listenerDashboard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ListenerBottomTabBar(
    selected: ListenerTab,
    onTabSelected: (ListenerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onTertiary,
        unselectedIconColor = MaterialTheme.colorScheme.secondary,
        selectedTextColor = MaterialTheme.colorScheme.onTertiary,
        unselectedTextColor = MaterialTheme.colorScheme.secondary,
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        NavigationBarItem(
            selected = selected == ListenerTab.DASHBOARD,
            onClick = { onTabSelected(ListenerTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard", style = MaterialTheme.typography.labelLarge) },
            colors = navBarColors
        )

        NavigationBarItem(
            selected = selected == ListenerTab.CALLS,
            onClick = { onTabSelected(ListenerTab.CALLS) },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("Calls", style = MaterialTheme.typography.labelLarge) },
            colors = navBarColors
        )

        NavigationBarItem(
            selected = selected == ListenerTab.SETTINGS,
            onClick = { onTabSelected(ListenerTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", style = MaterialTheme.typography.labelLarge) },
            colors = navBarColors
        )
    }
}
