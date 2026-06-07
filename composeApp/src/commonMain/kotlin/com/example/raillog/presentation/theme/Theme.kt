package com.example.raillog.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = RailLogColors.PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = RailLogColors.PrimaryNavyLight,
    onPrimaryContainer = Color.White,
    secondary = RailLogColors.SuccessEmerald,
    onSecondary = Color.White,
    background = RailLogColors.SurfaceSlate,
    onBackground = RailLogColors.TextPrimary,
    surface = Color.White,
    onSurface = RailLogColors.TextPrimary,
    error = RailLogColors.ErrorRed,
    onError = Color.White,
    outline = RailLogColors.BorderBlack
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB6C4FF),
    onPrimary = Color(0xFF00164E),
    primaryContainer = RailLogColors.PrimaryNavy,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9)
)

@Composable
fun RailLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(),
        shapes = shapes,
        content = content
    )
}
