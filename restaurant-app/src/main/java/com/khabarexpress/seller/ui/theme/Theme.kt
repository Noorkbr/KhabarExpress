package com.khabarexpress.seller.ui.theme

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

// Luxury Gold & Navy palette matching buyer app
private val Gold = Color(0xFFD4A03C)
private val GoldLight = Color(0xFFF5E6C8)
private val Navy = Color(0xFF1B2A4A)
private val NavyDark = Color(0xFF0F1D36)

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = Navy,
    primaryContainer = GoldLight,
    onPrimaryContainer = Navy,
    secondary = Navy,
    onSecondary = Color.White,
    background = Color(0xFFFCFAF6),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF7F3EC),
    onSurfaceVariant = Color(0xFF6B6B80),
    error = Color(0xFFE74C3C),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE8C068),
    onPrimary = Color.Black,
    primaryContainer = Gold,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF8B9DC3),
    onSecondary = Color.Black,
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6E1D8),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6E1D8),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF9E9E9E),
    error = Color(0xFFE74C3C),
    onError = Color.White
)

@Composable
fun RestaurantAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
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
        typography = MaterialTheme.typography,
        content = content
    )
}
