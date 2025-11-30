package com.example.app.ui.user

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserInfoScreen(
    modifier: Modifier
) {
    Text(
        text = "User Info Screen",
        style = MaterialTheme.typography.headlineMedium
    )

//    val vids = List(8) { idx -> "Video call $idx" }
//    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
//        items(vids) { v ->
//            Column(modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp)) {
//                Text(text = v, style = MaterialTheme.typography.titleMedium)
//                Text(text = "2 days ago", style = MaterialTheme.typography.bodySmall)
//                Divider(modifier = Modifier.padding(top = 8.dp))
//            }
//        }
//    }
}
