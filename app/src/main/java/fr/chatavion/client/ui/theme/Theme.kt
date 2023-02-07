package fr.chatavion.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = Black,
    onPrimary = White,
    primaryVariant = Blue,

    secondary = White,
    onSecondary = White,

    background = BlackBackground,
    onBackground = White,
    onSurface = White,
    surface = White,

    error = Red
)

private val LightColorPalette = lightColors(
    background = White,
//    primary = White,
    onPrimary = Black,
    onBackground = Black,
    onSurface = Black,

    primaryVariant = Gray,
    secondary = Black,
    surface = Black,
    onSecondary = Black,
)

/* Other colors to overwrite :
    primaryVariant,
    secondary,
    surface,
    onSecondary,
    ...
 */

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