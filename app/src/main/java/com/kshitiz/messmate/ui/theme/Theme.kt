package com.kshitiz.messmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.White

// --- Color Palettes ---
private val DarkColorScheme = darkColorScheme(
    primary = Verdigris,
    background = DarkJungleGreen,
    surface = GraniteGray,
    surfaceVariant = GraniteGray,
    onPrimary = Cultured,
    onBackground = Cultured,
    onSurface = Cultured,
    onSurfaceVariant = Cultured,
    primaryContainer = Verdigris,
    onPrimaryContainer = Cultured
)

private val LightColorScheme = lightColorScheme(
    primary = Verdigris,
    background = Cultured,
    surface = White,
    surfaceVariant = White,
    onPrimary = White,
    onBackground = DarkJungleGreen,
    onSurface = DarkJungleGreen,
    onSurfaceVariant = DarkJungleGreen,
    primaryContainer = Verdigris,
    onPrimaryContainer = White
)

@Composable
fun MessMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

