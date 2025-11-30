package com.example.app.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.chat.components.ChatMessageBubble
import com.example.app.ui.chat.components.ChatInputBar
import com.example.app.ui.chat.components.ChatHeader

@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit
) {
    // -------------------------------
    // TODO: Replace this with ViewModel call
    // val viewModel = hiltViewModel<ChatViewModel>()
    // val messages = viewModel.messages.collectAsState()
    // -------------------------------

    var messageList by remember {
        mutableStateOf(List(20) { idx ->
            ChatMessage(
                id = "$idx",
                text = "Message $idx from $chatId",
                isSender = idx % 2 == 0
            )
        })
    }

    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ChatHeader(
                title = "Contact $chatId",
                onBack = onBack
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = true, // WhatsApp-like scroll from bottom
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(chatId = "123", onBack = {})
    }
}
