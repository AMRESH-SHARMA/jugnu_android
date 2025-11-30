package com.example.app.ui.videocall

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
fun VideoCallListScreen(
    modifier: Modifier,
    onOpenVideo: (String) -> Unit = {}) {
    val vids = List(8) { idx -> "Video call $idx" }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
        items(vids) { v ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)) {
                Text(text = v, style = MaterialTheme.typography.titleMedium)
                Text(text = "2 days ago", style = MaterialTheme.typography.bodySmall)
                Divider(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
