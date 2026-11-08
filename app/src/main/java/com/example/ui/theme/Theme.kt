package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DjConsoleColorScheme = darkColorScheme(
    primary = DeckACyan,
    onPrimary = DjBackground,
    primaryContainer = DeckACyanDark,
    onPrimaryContainer = DeckACyanLight,
    secondary = DeckBAmber,
    onSecondary = DjBackground,
    secondaryContainer = DeckBAmberDark,
    onSecondaryContainer = DeckBAmberLight,
    tertiary = DjGreenActive,
    onTertiary = DjBackground,
    background = DjBackground,
    onBackground = DjTextPrimary,
    surface = DjSurface,
    onSurface = DjTextPrimary,
    surfaceVariant = DjSurfaceElevated,
    onSurfaceVariant = DjTextSecondary,
    outline = DjSurfaceBorder,
    error = DjRedCue
)

@Composable
fun AiDjMixerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DjConsoleColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AiDjMixerTheme(content = content)
}
