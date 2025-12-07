package com.example.app.preview

import androidx.compose.runtime.Composable
import com.example.app.ui.call.IncomingCallScreen
import com.example.app.ui.theme.AppTheme

@AllDevicesPreview
@Composable
fun IncomingCallScreenPreview() {
    AppTheme {
        IncomingCallScreen(
            callerName = "ABC",
            callerImage = "https://mdbcdn.b-cdn.net/img/new/avatars/2.webp",
            onAccept = {},
            onDecline = {}
        )
    }
}
