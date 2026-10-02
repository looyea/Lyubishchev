package com.lyubishchev.timekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private fun nightScheme(a: Accents) = darkColorScheme(
    primary = a.primary,
    onPrimary = a.onPrimary,
    primaryContainer = a.primaryContainer,
    onPrimaryContainer = a.onPrimaryContainer,
    secondary = a.secondary,
    onSecondary = a.onSecondary,
    secondaryContainer = a.secondaryContainer,
    onSecondaryContainer = a.onSecondaryContainer,
    tertiary = a.tertiary,
    tertiaryContainer = a.tertiaryContainer,
    onTertiaryContainer = a.onTertiaryContainer,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    onSurfaceVariant = NightMuted,
    surfaceVariant = NightSurfaceHigh,
    outline = NightOutline,
    outlineVariant = NightOutlineVariant,
    error = NightError,
    onError = NightOnError,
)

private fun dayScheme(a: Accents) = lightColorScheme(
    primary = a.primary,
    onPrimary = a.onPrimary,
    primaryContainer = a.primaryContainer,
    onPrimaryContainer = a.onPrimaryContainer,
    secondary = a.secondary,
    onSecondary = a.onSecondary,
    secondaryContainer = a.secondaryContainer,
    onSecondaryContainer = a.onSecondaryContainer,
    tertiary = a.tertiary,
    tertiaryContainer = a.tertiaryContainer,
    onTertiaryContainer = a.onTertiaryContainer,
    background = DayBackground,
    onBackground = DayText,
    surface = DaySurface,
    onSurface = DayText,
    onSurfaceVariant = DayMuted,
    surfaceVariant = DaySurfaceHigh,
    outline = DayOutline,
    outlineVariant = DayOutlineVariant,
    error = DayError,
    onError = DayOnError,
)

/**
 * 主题入口：颜色模式决定深浅，主题决定整套控件配色。跟随系统时看环境。
 * Theme entry: ThemeMode picks light/dark, ThemePalette swaps the accent set.
 */
@Composable
fun TimekeeperTheme(content: @Composable () -> Unit) {
    val dark = when (ThemePrefs.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = ThemePrefs.palette
    MaterialTheme(
        colorScheme = if (dark) nightScheme(palette.night) else dayScheme(palette.day),
        typography = TimekeeperTypography,
        content = content,
    )
}
