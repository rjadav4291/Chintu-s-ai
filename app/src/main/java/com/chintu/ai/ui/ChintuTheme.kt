package com.chintu.ai.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ChintuDarkColors = darkColorScheme(
    primary = ChintuColors.Primary,
    secondary = ChintuColors.Secondary,
    tertiary = ChintuColors.Accent,
    background = ChintuColors.Background,
    surface = ChintuColors.Surface,
    onPrimary = ChintuColors.OnPrimary,
    onSecondary = ChintuColors.OnPrimary,
    onBackground = ChintuColors.TextPrimary,
    onSurface = ChintuColors.TextPrimary
)

@Composable
fun ChintuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChintuDarkColors,
        content = content
    )
}
