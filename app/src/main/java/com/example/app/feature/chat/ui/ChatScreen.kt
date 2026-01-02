package com.example.app.feature.chat.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.feature.chat.ui.components.ChatHeader
import com.example.app.feature.chat.ui.components.ChatInputBar
import com.example.app.feature.chat.ui.components.ChatMessageBubble
//import com.example.app.domain.model.Listener
import com.example.app.feature.listeners.domain.ListenerModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    listenerModel: ListenerModel?,
    onBack: () -> Unit
) {
    val data = remember { listenerModel }

    if (data == null) return
    // -------------------------------
    // TODO: Replace this with ViewModel call
    // val viewModel = hiltViewModel<ChatViewModel>()
    // val messages = viewModel.messages.collectAsState()
    // -------------------------------

    var messageList by remember {
        mutableStateOf(List(20) { idx ->
            ChatMessage(
                id = "$idx",
                text = "Message $idx from aaa",
                isSender = idx % 2 == 0
            )
        })
    }

    var input by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatHeader(
                listenerModel = data,
                onVoiceCall = {
                    // navController.navigate("incoming_call/$chatId")
                },
                onVideoCall = {
                    // navController.navigate("incoming_video_call/$chatId")
                },
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            ChatInputBar(
                value = input,
                onValueChange = { input = it },
                onSendClick = {
                    if (input.isNotBlank()) {
                        // -------------------------------
                        // TODO: send message to backend
                        // viewModel.sendMessage(input)
                        // -------------------------------

                        messageList = messageList + ChatMessage(
                            id = (messageList.size + 1).toString(),
                            text = input,
                            isSender = true
                        )
                        input = ""
                    }
                }
            )
        },

        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = true,
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messageList.reversed(), key = { it.id }) { msg ->
                ChatMessageBubble(message = msg)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isSender: Boolean
)
