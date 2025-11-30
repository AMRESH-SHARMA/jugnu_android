package com.example.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WhatsAppDarkBackground = Color(0xFF121C21)
val WhatsAppTopBar = Color(0xFF1F2C34)
val WhatsAppBubbleReceived = Color(0xFF262D31)
val WhatsAppBubbleSent = Color(0xFF056162)
val WhatsAppFAB = Color(0xFF075E54)
val WhatsAppTextPrimary = Color(0xFFECE5DD)
val WhatsAppTextSecondary = Color(0xFFAEBAC1)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppFAB,
    secondary = WhatsAppBubbleSent,
    background = WhatsAppDarkBackground,
    surface = WhatsAppTopBar,
    onPrimary = WhatsAppTextPrimary,
    onBackground = WhatsAppTextPrimary,
    onSurface = WhatsAppTextPrimary
)

@Composable
fun AppTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

//@Composable
//fun AppTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}