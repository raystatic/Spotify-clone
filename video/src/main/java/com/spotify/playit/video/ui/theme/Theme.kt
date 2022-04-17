package com.spotify.playit.video.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = ColorPrimary,
    primaryVariant = ColorAccent,
    secondary = ColorAccent,
    background = ColorPrimaryDark
)

private val LightColorPalette = lightColors(
    primary = ColorPrimary,
    primaryVariant = ColorAccent,
    secondary = ColorAccent,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun SpotifyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }

    val colors = DarkColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography(fontFamily),
        shapes = Shapes,
        content = content
    )
}