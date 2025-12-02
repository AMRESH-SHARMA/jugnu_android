package com.example.app.ui.call

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app.ui.call.components.ListenersSearchBar

@Composable
fun CallListScreen(
    modifier: Modifier = Modifier,
    onOpenCall: (String) -> Unit = {}
) {
    // Search Query state inside this screen
    var searchQuery by remember { mutableStateOf("") }

    // Dummy calls
    val calls = List(12) { idx -> "Call item $idx" }

    // Apply search filter
    val filteredCalls = calls.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        // ⭐ Listeners Search Bar
        ListenersSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            hint = "Search call contacts"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ⭐ List of calls
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredCalls) { call ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = call,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }

            item {
                Text(
                    text = "last item in the scrollable list.",
                    textAlign = TextAlign.Center,

                    )
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}