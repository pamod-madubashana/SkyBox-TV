package com.skybox.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Color scheme derived from the Color object.
 * Provides the color palette for the TV theme.
 */
val colorScheme = lightColorScheme(
    primary = Color.magenta600,
    onPrimary = Color.white,
    secondary = Color.cyan400,
    onSecondary = Color.black,
    background = Color.black,
    onBackground = Color.white,
    surface = Color.black,
    onSurface = Color.white
)

/**
 * TvTheme composition local that sets up TV-optimized theming.
 * This provides the default TV theming via AndroidX tv-material library.
 */
@Composable
fun TvTheme(
    content: @Composable () -> Unit
) {
    // AndroidX TvTheme provides TV-specific theming defaults
    // including proper focus handling, backdrop colors, etc.
    androidx.tv.material.TvTheme(
        content = content,
        colorScheme = colorScheme
    )
}