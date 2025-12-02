package com.example.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GREEN200 = Color(0xFFe2ffc7)
val GREEN300 = Color(0xFF7eb0ad)
val GREEN400 = Color(0xFF16cc3e)
val GREEN450 = Color(0xFF1AA05B)
val GREEN500 = Color(0xFF19887a)
val GREEN600 = Color(0xFF0e5e55)
val GREEN700 = Color(0xFF00574B)

val DARK_GREEN200 = Color(0xFF232D36)
val DARK_GREEN300 = Color(0xFF101D25)

val WHITE200 = Color(0xFFe0e0e0)
val BLACK200 = Color(0xFA212020)
val GRAY100 = Color(0xC1EFF0F3)
val GRAY200 = Color(0xFF88898b)

val shimmerHighLight = Color(0xA3C2C2C2)

@Composable
fun getTabPrimaryColor(): Color {
    return if (isSystemInDarkTheme()) {
        GREEN450
    } else {
        WHITE200
    }
}

@Composable
fun getTitleColor(): Color {
    return if (isSystemInDarkTheme()) {
        WHITE200
    } else {
        BLACK200
    }
}
