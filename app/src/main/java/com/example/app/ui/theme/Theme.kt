package com.example.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = DARK_GREEN200,
    primaryContainer = DARK_GREEN300,
    secondaryContainer = GRAY200,
    background = DARK_GREEN300,
    surface = DARK_GREEN300,
    secondary = GRAY100,
    tertiary = GRAY200,
    onTertiary = WHITE200,
)

private val DarkAndroidBackgroundTheme = BackgroundTheme(color = DARK_GREEN300)

@Composable
fun AppTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(),
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
