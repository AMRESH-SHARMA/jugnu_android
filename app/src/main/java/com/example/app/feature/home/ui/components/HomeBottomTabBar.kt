package com.example.app.feature.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.unit.Dp
import com.example.app.feature.home.ui.HomeTab

@Composable
fun HomeBottomTabBar(
    selected: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp
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
            selected = selected == HomeTab.LISTENERS,
            onClick = { onTabSelected(HomeTab.LISTENERS) },
            icon = { Icon(Icons.Default.Call, contentDescription = "Listeners") },
            label = {
                Text(
                    "Listeners",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            colors = navBarColors
        )
        
        NavigationBarItem(
            selected = selected == HomeTab.RECENTS,
            onClick = { onTabSelected(HomeTab.RECENTS) },
            icon = { Icon(Icons.Default.History, contentDescription = "Recents") },
            label = {
                Text(
                    "Recents",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            colors = navBarColors
        )
        
        NavigationBarItem(
            selected = selected == HomeTab.USER,
            onClick = { onTabSelected(HomeTab.USER) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            colors = navBarColors
        )
    }
}
