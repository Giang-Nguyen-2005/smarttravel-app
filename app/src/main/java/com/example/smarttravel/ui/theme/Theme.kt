package com.example.smarttravel.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212), // Nền tối
    surface = Color(0xFF1E1E1E), // Surface tối
    onBackground = Color(0xFFFFFFFF), // Text trên nền tối
    onSurface = Color(0xFFFFFFFF), // Text trên surface tối
    surfaceVariant = Color(0xFF2C2C2C), // Surface variant cho cards
    onSurfaceVariant = Color(0xFFB0B0B0), // Text trên surface variant
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF5F7FA), // Nền sáng
    surface = Color(0xFFFFFFFF), // Surface sáng
    onBackground = Color(0xFF1A1A1A), // Text trên nền sáng
    onSurface = Color(0xFF1A1A1A), // Text trên surface sáng
    surfaceVariant = Color(0xFFF0F0F0), // Surface variant cho cards
    onSurfaceVariant = Color(0xFF757575), // Text trên surface variant
)

@Composable
fun SmarttravelTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDarkTheme
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}