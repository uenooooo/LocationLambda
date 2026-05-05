package com.yasumo.locationlambda.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Slate,
    onPrimary = CardSurface,
    secondary = AccentSand,
    onSecondary = Slate,
    background = SandBackground,
    onBackground = Slate,
    surface = CardSurface,
    onSurface = Slate
)

private val DarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    secondary = SlateSoft,
    background = DarkBackground,
    onBackground = CardSurface,
    surface = DarkSurface,
    onSurface = CardSurface
)

@Composable
fun LocationLambdaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
