package com.example.samsungcontroll.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** The remote is a dark-only instrument; Material components inherit the same tokens. */
private val RemoteColorScheme = darkColorScheme(
    primary = RemoteTokens.Accent,
    onPrimary = RemoteTokens.AccentInk,
    secondary = RemoteTokens.Accent,
    onSecondary = RemoteTokens.AccentInk,
    tertiary = RemoteTokens.Warn,
    background = RemoteTokens.Background,
    onBackground = RemoteTokens.TextPrimary,
    surface = RemoteTokens.Surface1,
    onSurface = RemoteTokens.TextPrimary,
    surfaceVariant = RemoteTokens.Surface2,
    onSurfaceVariant = RemoteTokens.TextSecondary,
    surfaceContainer = RemoteTokens.Surface1,
    surfaceContainerHigh = RemoteTokens.Surface2,
    surfaceContainerHighest = RemoteTokens.Surface3,
    outline = RemoteTokens.BorderStrong,
    outlineVariant = RemoteTokens.Border,
    error = RemoteTokens.Power
)

@Composable
fun SamsungControllTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RemoteColorScheme,
        typography = Typography,
        content = content
    )
}
