package com.spendsense.app.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = MintEmerald,
    secondary = MintEmeraldLight,
    tertiary = SaveBlue,
    background = SlateDarkBG,
    surface = SlateSurface,
    surfaceVariant = SlateSurfaceCard,
    onPrimary = SlateDarkBG,
    onSecondary = SlateDarkBG,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextLight,
    error = SpendRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF009B74), // Slightly darker green for light mode readability
    secondary = Color(0xFF007A5B),
    tertiary = Color(0xFF0288D1),
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155),
    error = Color(0xFFD32F2F)
)

@Composable
fun SpendSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = Typography,
        content = content
    )
}
