package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = Color.Black,
    secondary = TrophyGold,
    onSecondary = Color.Black,
    tertiary = PitchBlue,
    background = StadiumDark,
    surface = StadiumSurface,
    onPrimaryContainer = IceWhite,
    onBackground = IceWhite,
    onSurface = IceWhite,
    outline = StadiumBorder,
    error = RedCard
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Premium Sports styling
    dynamicColor: Boolean = false, // Disable to preserve our bespoke stadium green theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
