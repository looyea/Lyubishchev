package com.lyubishchev.timekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * surface / surfaceHigh 之间的一步混合，用来派生 Material3 1.4 的 surface 容器档位。
 * （ui-graphics 1.12 没有公开的 Color.blend，这里用同义的 lerp。）
 */
private fun blend(from: Color, to: Color, fraction: Float): Color = lerp(from, to, fraction)

private fun nightScheme(a: Accents, n: Neutrals) = darkColorScheme(
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
    background = n.background,
    onBackground = n.text,
    surface = n.surface,
    onSurface = n.text,
    onSurfaceVariant = n.muted,
    surfaceVariant = n.surfaceHigh,
    surfaceContainerLowest = n.surface,
    surfaceContainerLow = blend(n.surface, n.surfaceHigh, 0.30f),
    surfaceContainer = blend(n.surface, n.surfaceHigh, 0.55f),
    surfaceContainerHigh = blend(n.surface, n.surfaceHigh, 0.78f),
    surfaceContainerHighest = n.surfaceHigh,
    surfaceDim = n.background,
    surfaceBright = blend(n.surface, n.surfaceHigh, 0.12f),
    outline = n.outline,
    outlineVariant = n.outlineVariant,
    error = NightError,
    onError = NightOnError,
)

private fun dayScheme(a: Accents, n: Neutrals) = lightColorScheme(
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
    background = n.background,
    onBackground = n.text,
    surface = n.surface,
    onSurface = n.text,
    onSurfaceVariant = n.muted,
    surfaceVariant = n.surfaceHigh,
    surfaceContainerLowest = n.surface,
    surfaceContainerLow = blend(n.surface, n.surfaceHigh, 0.30f),
    surfaceContainer = blend(n.surface, n.surfaceHigh, 0.55f),
    surfaceContainerHigh = blend(n.surface, n.surfaceHigh, 0.78f),
    surfaceContainerHighest = n.surfaceHigh,
    surfaceDim = n.background,
    surfaceBright = blend(n.surface, n.surfaceHigh, 0.12f),
    outline = n.outline,
    outlineVariant = n.outlineVariant,
    error = DayError,
    onError = DayOnError,
)

/**
 * 主题入口：颜色模式决定深浅，主题决定整套配色（控件色 + 染色中性层）。跟随系统时看环境。
 * Theme entry: ThemeMode picks light/dark, ThemePalette swaps both the accents and the tinted neutrals.
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
        colorScheme = if (dark) nightScheme(palette.night, palette.nightNeutrals)
        else dayScheme(palette.day, palette.dayNeutrals),
        typography = TimekeeperTypography,
        content = content,
    )
}
