package com.example.app.root

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun ForceUpdateScreen(
    message: String?,
    playStoreUrl: String?,
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primaryContainer)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Update Required",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onPrimaryContainer
            )

            Text(
                text = message
                    ?: "A new version of the app is available. Please update to continue.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colors.onPrimaryContainer
            )

            Button(
                onClick = {
                    val url = playStoreUrl
                        ?: "https://play.google.com/store/apps/details?id=com.amresh.helium"

                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tertiary,
                    contentColor = colors.onTertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Now")
            }
        }
    }
}
