package com.lyubishchev.timekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = InkPrimary,
    onPrimary = InkOnPrimary,
    primaryContainer = InkPrimaryContainer,
    onPrimaryContainer = InkOnPrimaryContainer,
    secondary = InkSecondary,
    onSecondary = Color(0xFF0A1620),
    secondaryContainer = InkSecondaryContainer,
    onSecondaryContainer = InkOnSecondaryContainer,
    tertiary = InkAccent,
    tertiaryContainer = InkTertiaryContainer,
    onTertiaryContainer = InkOnTertiaryContainer,
    background = InkBackground,
    onBackground = InkText,
    surface = InkSurface,
    onSurface = InkText,
    onSurfaceVariant = InkMuted,
    surfaceVariant = InkSurfaceHigh,
    outline = InkOutline,
    outlineVariant = InkOutlineVariant,
    error = InkError,
    onError = Color(0xFF330000),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E7A6C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBDF2EA),
    onPrimaryContainer = Color(0xFF00332C),
    secondary = Color(0xFF3D5A73),
    secondaryContainer = Color(0xFFD6E6F5),
    onSecondaryContainer = Color(0xFF0A1C2A),
    tertiary = Color(0xFF9A5B12),
    tertiaryContainer = Color(0xFFFFE0B8),
    onTertiaryContainer = Color(0xFF321B00),
    background = PaperBackground,
    onBackground = PaperText,
    surface = PaperSurface,
    onSurface = PaperText,
    onSurfaceVariant = PaperMuted,
    surfaceVariant = PaperSurfaceHigh,
    outline = Color(0xFFCBD2D9),
    outlineVariant = Color(0xFFE3E8ED),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFDAD5),
)

/**
 * 主题入口：按用户在「我的」里的选择决定深浅色，跟随系统时看环境。
 * Theme entry: honours the stored ThemeMode; SYSTEM defers to the platform.
 */
@Composable
fun TimekeeperTheme(content: @Composable () -> Unit) {
    val dark = when (ThemePrefs.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = TimekeeperTypography,
        content = content,
    )
}
