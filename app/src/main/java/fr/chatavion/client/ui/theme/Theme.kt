package fr.chatavion.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = Colors(
    primary = Black,
    onPrimary = White,
    primaryVariant = Gray,

    secondary = Blue,
    onSecondary = White,
    secondaryVariant = White,

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
    primaryVariant = LightGray,

    secondary = Gray,
    onSecondary = White,
    secondaryVariant = White,

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
    darkThemeEnabled: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkThemeEnabled) {
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