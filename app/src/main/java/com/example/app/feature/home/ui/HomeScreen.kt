package com.example.app.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.example.app.feature.chat.ui.ChatListScreen
import com.example.app.feature.home.ui.components.HomeBottomTabBar
import com.example.app.feature.home.ui.components.HomeTopBar
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.ui.ListenerListScreen
import com.example.app.feature.navigation.ui.Routes
import com.example.app.feature.user.ui.UserInfoScreen

enum class HomeTab { LISTENERS, CHATS, USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,                            // required param
    initialTab: HomeTab = HomeTab.LISTENERS,                 // default param
    onContactClick: (HomeTab, ListenerModel) -> Unit = { _, _ -> }, // lambda
    modifier: Modifier = Modifier
) {

    var currentTab by remember { mutableStateOf(initialTab) }
    Scaffold(
        topBar = {
            HomeTopBar(
                title = when (currentTab) {
                    HomeTab.LISTENERS -> "Listeners"
                    HomeTab.CHATS -> "Chats"
                    HomeTab.USER -> "User"
                },
                showWalletSection = currentTab != HomeTab.USER,
                balance = "₹1200",
                onWalletClick = { navController.navigate(Routes.WALLET) }
            )
//            HorizontalDivider()
        },
        bottomBar = {
            HomeBottomTabBar(
                selected = currentTab,
                onTabSelected = { selected -> currentTab = selected },
                elevation = 0.dp,
                modifier = Modifier
                    .height(120.dp)
//                    .navigationBarsPadding()
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
                            onOpenChat = { }
                        )

                        HomeTab.LISTENERS -> ListenerListScreen(
                            modifier = Modifier.fillMaxSize(),
                            navController = navController,
                            onOpenListener = { listener ->
                                onContactClick(HomeTab.CHATS, listener)
                            }
                        )

                        HomeTab.USER -> UserInfoScreen(
//                            modifier = Modifier.fillMaxSize()
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
