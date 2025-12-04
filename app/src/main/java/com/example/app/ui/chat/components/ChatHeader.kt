package com.example.app.ui.chat.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChatHeader(
//    title: String,
//    onBack: () -> Unit
//) {
//    TopAppBar(
//        title = { Text(title) },
//        navigationIcon = {
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowOutward, contentDescription = "Back")
//            }
//        }
//    )
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    title: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        // ⭐ MAKE TOP BAR STICKY
        windowInsets = WindowInsets.statusBars,

        // Optional: black header like WhatsApp
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Black,
//            titleContentColor = Color.White,
//            navigationIconContentColor = Color.White
//        )
    )
}
