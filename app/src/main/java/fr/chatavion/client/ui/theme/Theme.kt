package fr.chatavion.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = Colors(
    primary = Black,
    onPrimary = White,
    primaryVariant = Blue,

    secondary = Blue,
    onSecondary = White,
    secondaryVariant = Blue,

    background = BlackBackground,
    onBackground = White,

    surface = White,
    onSurface = White,

    error = Red,
    onError = Red,

    isLight = false
)

private val LightColorPalette = Colors(
    primary = White,
    onPrimary = Black,
    primaryVariant = Gray,

    secondary = Gray,
    onSecondary = White,
    secondaryVariant = Gray,

    background = White,
    onBackground = Black,

    surface = Black,
    onSurface = Black,

    error = Red,
    onError = Red,

    isLight = false
)

@Composable
fun ChatavionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}