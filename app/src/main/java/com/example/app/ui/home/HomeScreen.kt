package com.example.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.ui.call.CallListScreen
import com.example.app.ui.chat.ChatListScreen
import com.example.app.ui.home.components.HomeBottomTabBar
import com.example.app.ui.home.components.HomeTopBar
import com.example.app.ui.navigation.Routes
import com.example.app.ui.user.UserInfoScreen

enum class HomeTab { CALLS, CHATS, USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,                         // required param
    initialTab: HomeTab = HomeTab.CALLS,                 // default param
    onContactClick: (HomeTab, String) -> Unit = { _, _ -> }, // lambda
    modifier: Modifier = Modifier                         // modifier last
) {

    var currentTab by remember { mutableStateOf(initialTab) }
    Scaffold(
        topBar = {
            HomeTopBar(
                title = when (currentTab) {
                    HomeTab.CALLS -> "Calls"
                    HomeTab.CHATS -> "Chats"
                    HomeTab.USER -> "User"
                },
                showWalletSection = currentTab != HomeTab.USER,
                balance = "₹1200",
                onWalletClick = { navController.navigate(Routes.WALLET) }
            )
        },
        bottomBar = {
            HomeBottomTabBar(
                selected = currentTab,
                onTabSelected = { selected -> currentTab = selected },
                elevation = 0.dp,
                modifier = Modifier
                    .height(100.dp)
                    .navigationBarsPadding()
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {

            // Scrollable Main Content
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {   // NEW: ensures LazyColumn gets full height
                    when (currentTab) {
                        HomeTab.CHATS -> ChatListScreen(
                            modifier = Modifier.fillMaxSize(),
                            onOpenChat = { id -> onContactClick(HomeTab.CHATS, id) }
                        )

                        HomeTab.CALLS -> CallListScreen(
                            modifier = Modifier.fillMaxSize()
                        )

                        HomeTab.USER -> UserInfoScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/** Drawer Content */
//@Composable
//private fun HomeDrawerContent(onClose: () -> Unit) {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text(text = "Menu", style = MaterialTheme.typography.headlineSmall)
//
//        Spacer(modifier = Modifier.height(8.dp))
//        TextButton(onClick = onClose) { Text("Close") }
//
//        Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//        Text("Profile")
//        Spacer(modifier = Modifier.height(6.dp))
//        Text("Settings")
//        Spacer(modifier = Modifier.height(6.dp))
//        Text("Help")
//    }
//}


/** Sample contact data */
data class Contact(val id: String, val name: String, val avatarColorHex: Long)

private fun sampleContacts(): List<Contact> {
    return List(12) { index ->
        Contact(id = "c$index", name = "Contact $index", avatarColorHex = 0xFFCCCCCC)
    }
}
