package com.example.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

//val background = Color("#075E54".toColorInt())
//
//val WhatsAppDarkBackground = Color("#075E54".toColorInt())
//val WhatsAppTopBar = Color("#075E54".toColorInt())
//val WhatsAppBubbleReceived = Color(0xFF262D31)
//val WhatsAppBubbleSent = Color(0xFF056162)
//val WhatsAppFAB = Color(0xFF075E54)
//val WhatsAppTextPrimary = Color(0xFFECE5DD)
//val WhatsAppTextSecondary = Color(0xFFAEBAC1)

private val DarkColorScheme = darkColorScheme(
    primary = DARK_GREEN200,
    primaryContainer = DARK_GREEN300,
    background = DARK_GREEN300,
    surface = DARK_GREEN300,
    secondary = GREEN500,
    tertiary = WHITE200,
    onTertiary = GRAY200,
)

private val DarkAndroidBackgroundTheme = BackgroundTheme(color = DARK_GREEN300)

@Composable
fun AppTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalBackgroundTheme provides DarkAndroidBackgroundTheme
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }

}
