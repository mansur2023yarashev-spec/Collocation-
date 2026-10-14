package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPlumAccent,
    onPrimary = Color.Black,
    primaryContainer = DarkPlumRow,
    onPrimaryContainer = DarkPlumTextPrimary,
    secondary = DarkPlumAccent,
    onSecondary = Color.Black,
    secondaryContainer = DarkPlumRow,
    onSecondaryContainer = DarkPlumTextPrimary,
    tertiary = DarkPlumAccent,
    background = DarkPlumBg,
    onBackground = DarkPlumTextPrimary,
    surface = DarkPlumCard,
    onSurface = DarkPlumTextPrimary,
    surfaceVariant = DarkPlumRow,
    onSurfaceVariant = DarkPlumTextSecondary,
    outline = DarkPlumBorder,
    outlineVariant = DarkPlumRowBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightMauveAccent,
    onPrimary = Color.White,
    primaryContainer = LightMauveRow,
    onPrimaryContainer = LightMauveTextPrimary,
    secondary = LightMauveAccent,
    onSecondary = Color.White,
    secondaryContainer = LightMauveRow,
    onSecondaryContainer = LightMauveTextPrimary,
    tertiary = LightMauveAccent,
    background = LightMauveBg,
    onBackground = LightMauveTextPrimary,
    surface = LightMauveCard,
    onSurface = LightMauveTextPrimary,
    surfaceVariant = LightMauveRow,
    onSurfaceVariant = LightMauveTextSecondary,
    outline = LightMauveBorder,
    outlineVariant = LightMauveRowBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
