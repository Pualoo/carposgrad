package com.example.myapitest.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    secondary = ColorPrimaryDark,
    tertiary = ColorAccent,
    background = BackgroundDark,
    surface = BackgroundDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextColorDark,
    onSurface = TextColorDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    secondary = ColorPrimaryDark,
    tertiary = ColorAccent,
    background = BackgroundLight,
    surface = BackgroundLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextColorLight,
    onSurface = TextColorLight,

    )

@Composable
fun MyApiTestTheme(
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
