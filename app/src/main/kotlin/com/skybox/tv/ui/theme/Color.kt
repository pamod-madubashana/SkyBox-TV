package com.skybox.tv.ui.theme

import com.google.accompanist.material.TvColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Simple color palette for TV UI
object Color {
    val magenta600 = androidx.compose.ui.graphics.Color("#FF006064")
    val cyan400 = androidx.compose.ui.graphics.Color("#FF00BCD4")
    val white = androidx.compose.ui.graphics.Color("#FFFFFFFF")
    val black = androidx.compose.ui.graphics.Color("#FF000000")
}

// TvColorScheme composition for TV theming
val tvColorScheme: TvColorScheme =TvColorScheme(
    primary = Color.magenta600,
    onPrimary = Color.white,
    secondary = Color.cyan400,
    onSecondary = Color.black,
    background = Color.black,
    onBackground = Color.white,
    surface = Color.black,
    onSurface = Color.white
)