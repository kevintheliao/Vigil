package com.example.vigil.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val VigilLightColorScheme = lightColorScheme(
    primary = VigilPrimary,
    onPrimary = VigilOnPrimary,
    primaryContainer = VigilPrimaryContainer,
    onPrimaryContainer = VigilOnPrimaryContainer,
    secondary = VigilSecondary,
    secondaryContainer = VigilSecondaryContainer,
    onSecondaryContainer = VigilOnSecondaryContainer,
    tertiary = VigilTertiary,
    tertiaryContainer = VigilTertiaryContainer,
    error = VigilError,
    errorContainer = VigilErrorContainer,
    onErrorContainer = VigilOnErrorContainer,
    background = VigilBackground,
    onBackground = VigilOnSurface,
    surface = VigilSurface,
    onSurface = VigilOnSurface,
    surfaceVariant = VigilSurfaceVariant,
    onSurfaceVariant = VigilOnSurfaceVariant,
    surfaceContainerLowest = VigilSurfaceContainerLowest,
    surfaceContainerLow = VigilSurfaceContainerLow,
    surfaceContainer = VigilSurfaceContainer,
    surfaceContainerHigh = VigilSurfaceContainerHigh,
    outline = VigilOutline,
    outlineVariant = VigilOutlineVariant,
)

@Composable
fun VigilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand palette overrides dynamic color by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else VigilLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
