package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SpeedDarkColorScheme = darkColorScheme(
    primary = FlagshipCyan,
    secondary = FlagshipOrange,
    tertiary = GamingViolet,
    background = AmoledBlack,
    surface = DarkCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = SlateGrey,
    outline = DarkCardBorder
)

// Standard fallback if user demands light system layout, still retaining elegant luxury black card accents.
private val SpeedLightColorScheme = lightColorScheme(
    primary = FlagshipDeepCyan,
    secondary = FlagshipDeepOrange,
    tertiary = GamingViolet,
    background = Color(0xFFF6F8FA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F0F11),
    onSurface = Color(0xFF0F0F11),
    surfaceVariant = Color(0xFFEDEDF2),
    onSurfaceVariant = Color(0xFF5F5F6F),
    outline = Color(0x3B000000)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force amoled dark by default for the premium "Speed" visual appeal
    dynamicColor: Boolean = false, // Keep themed brand colors static for luxurious Apple-styled unified accent control
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        SpeedDarkColorScheme
    } else {
        SpeedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
