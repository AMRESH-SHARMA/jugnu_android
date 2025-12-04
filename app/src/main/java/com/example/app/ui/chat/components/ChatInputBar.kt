package com.example.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 10.dp,
//                bottom = 16.dp
            )
//            .background(Color.Black)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ⭐ WhatsApp-style rounded grey input box
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = Color(0xFF1F1F1F),           // WhatsApp dark grey
                    shape = RoundedCornerShape(28.dp)   // rounded bubble
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = SolidColor(Color.White),
                maxLines = 4,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Type a message",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

//        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}