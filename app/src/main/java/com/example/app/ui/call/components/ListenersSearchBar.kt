package com.example.app.ui.call.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(text = hint) },
        singleLine = true,
        shape = RoundedCornerShape(25.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
//            unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
//            focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
//            disabledBorderColor = MaterialTheme.colorScheme.background,
//            unfocusedBorderColor = MaterialTheme.colorScheme.background,
//            focusedBorderColor = MaterialTheme.colorScheme.background
        ),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
    )
}
