package com.example.macaveavin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Wine-inspired static color palettes (used when dynamic color is not available or disabled)
private val WineLightColors = lightColorScheme(
    primary = Color(0xFF7B1E3B),        // deep burgundy
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD8E4),
    onPrimaryContainer = Color(0xFF2F0013),
    secondary = Color(0xFF9C7A00),      // gold accents
    onSecondary = Color(0xFF1F1400),
    secondaryContainer = Color(0xFFFFE086),
    onSecondaryContainer = Color(0xFF251A00),
    tertiary = Color(0xFF6E4B8E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1F1A1C),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1F1A1C),
    surfaceVariant = Color(0xFFF3DDE3),
    onSurfaceVariant = Color(0xFF514347),
    outline = Color(0xFF837377)
)

private val WineDarkColors = darkColorScheme(
    primary = Color(0xFFFFB0C8),
    onPrimary = Color(0xFF4A0B23),
    primaryContainer = Color(0xFF631631),
    onPrimaryContainer = Color(0xFFFFD8E4),
    secondary = Color(0xFFFFC940),
    onSecondary = Color(0xFF3F2F00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFE086),
    tertiary = Color(0xFFD5BAFF),
    onTertiary = Color(0xFF3B2959),
    background = Color(0xFF1F1A1C),
    onBackground = Color(0xFFEAE0E4),
    surface = Color(0xFF1F1A1C),
    onSurface = Color(0xFFEAE0E4),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFE6C9D0),
    outline = Color(0xFFA08B91)
)

// Material 3 Typography scale (kept default to avoid functional changes)
private val AppTypography = Typography()

// Modern rounded shapes for an up-to-date look and better touch ergonomics
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        useDarkTheme -> WineDarkColors
        else -> WineLightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
