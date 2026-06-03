package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AuraTeal,
    secondary = AuraPurple,
    tertiary = AuraGlowGold,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = NeutralLight,
    onSurface = NeutralLight,
    onSurfaceVariant = NeutralLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Premium Dark Mode by default for eye-saving aura focus
    dynamicColor: Boolean = false, // Use our strictly customized glowing colors
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
