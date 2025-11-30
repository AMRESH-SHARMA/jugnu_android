package com.example.app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    onOpenChat: (String) -> Unit = {}
) {
    val messages = List(20) { idx -> "Chat message $idx" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(messages) { msg ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onOpenChat(msg) }   // ★ navigate with msg (or chatId)
            ) {
                Text(text = "User", style = MaterialTheme.typography.titleMedium)
                Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                Divider(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

