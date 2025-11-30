package com.example.app.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.ui.home.Contact


@Composable
fun HorizontalContactsRow(
    contacts: List<Contact>,
    onContactClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onContactClick(contact.id) }
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp),
                    color = Color(0xFFBBDEFB) // placeholder pastel color
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = contact.name.take(1), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = contact.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}
