package com.example.app.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.chat.ChatMessage

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSender) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isSender)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(message.text)
            }
        }
    }
}
