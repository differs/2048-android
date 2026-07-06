package com.differs.game2048.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentDark,
    onPrimary = TextLight,
    background = Warm50,
    onBackground = TextDark,
    surface = Warm100,
    onSurface = TextDark,
    surfaceVariant = EmptyCell,
    secondary = BoardBg
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = DarkBg,
    background = DarkBg,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkEmptyCell,
    secondary = DarkBoardBg
)

@Composable
fun Game2048Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
