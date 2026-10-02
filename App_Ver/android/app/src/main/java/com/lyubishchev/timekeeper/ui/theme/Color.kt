package com.lyubishchev.timekeeper.ui.theme

import androidx.compose.ui.graphics.Color

// ── 莫兰迪极简：暖米白 / 炭灰底是全局中性层，「主题」只换控件配色那一组 ──
// Morandi neutrals are the global base; a palette swaps only primary/secondary/tertiary.

// 浅色 / day
val DayBackground = Color(0xFFF7F4EE)
val DaySurface = Color(0xFFFCFAF5)
val DaySurfaceHigh = Color(0xFFEDE8DE)
val DayText = Color(0xFF33302B)
val DayMuted = Color(0xFF7C7568)
val DayOutline = Color(0xFFB4ACA0)
val DayOutlineVariant = Color(0xFFE4DED2)
val DayError = Color(0xFFB3261E)
val DayOnError = Color(0xFFFFDAD5)

// 深色 / night
val NightBackground = Color(0xFF201E1A)
val NightSurface = Color(0xFF292722)
val NightSurfaceHigh = Color(0xFF35322B)
val NightText = Color(0xFFE9E5DB)
val NightMuted = Color(0xFFA69E90)
val NightOutline = Color(0xFF57524A)
val NightOutlineVariant = Color(0xFF3A362F)
val NightError = Color(0xFFF28B82)
val NightOnError = Color(0xFF601410)

/** 一套控件配色：主/辅/第三色及其容器。切换主题就是整组换掉。 */
data class Accents(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

/** 低饱和色相轮：只给色相，明度和饱和度按深浅色模式定死，保证和莫兰迪中性层同气质。 */
private fun accentsDay(h1: Float, h2: Float, h3: Float) = Accents(
    primary = Color.hsl(h1, 0.26f, 0.40f),
    onPrimary = Color.hsl(h1, 0.30f, 0.96f),
    primaryContainer = Color.hsl(h1, 0.24f, 0.87f),
    onPrimaryContainer = Color.hsl(h1, 0.30f, 0.20f),
    secondary = Color.hsl(h2, 0.30f, 0.45f),
    onSecondary = Color.hsl(h2, 0.32f, 0.96f),
    secondaryContainer = Color.hsl(h2, 0.30f, 0.88f),
    onSecondaryContainer = Color.hsl(h2, 0.34f, 0.22f),
    tertiary = Color.hsl(h3, 0.22f, 0.44f),
    tertiaryContainer = Color.hsl(h3, 0.24f, 0.89f),
    onTertiaryContainer = Color.hsl(h3, 0.26f, 0.21f),
)

private fun accentsNight(h1: Float, h2: Float, h3: Float) = Accents(
    primary = Color.hsl(h1, 0.24f, 0.72f),
    onPrimary = Color.hsl(h1, 0.30f, 0.14f),
    primaryContainer = Color.hsl(h1, 0.20f, 0.30f),
    onPrimaryContainer = Color.hsl(h1, 0.24f, 0.87f),
    secondary = Color.hsl(h2, 0.30f, 0.68f),
    onSecondary = Color.hsl(h2, 0.34f, 0.14f),
    secondaryContainer = Color.hsl(h2, 0.22f, 0.32f),
    onSecondaryContainer = Color.hsl(h2, 0.28f, 0.86f),
    tertiary = Color.hsl(h3, 0.20f, 0.68f),
    tertiaryContainer = Color.hsl(h3, 0.20f, 0.32f),
    onTertiaryContainer = Color.hsl(h3, 0.24f, 0.87f),
)

// 默认：灰绿 + 陶土 + 灰紫（v0.8 起的手调值，保持不动）
val MorandiDay = Accents(
    primary = Color(0xFF5F7361),
    onPrimary = Color(0xFFF4F7F1),
    primaryContainer = Color(0xFFDCE4D7),
    onPrimaryContainer = Color(0xFF2B382D),
    secondary = Color(0xFFA98069),
    onSecondary = Color(0xFFFBF4EE),
    secondaryContainer = Color(0xFFEDDFD2),
    onSecondaryContainer = Color(0xFF432D20),
    tertiary = Color(0xFF84798F),
    tertiaryContainer = Color(0xFFE6DFEC),
    onTertiaryContainer = Color(0xFF372F41),
)

val MorandiNight = Accents(
    primary = Color(0xFFA4BCA6),
    onPrimary = Color(0xFF1F2B21),
    primaryContainer = Color(0xFF3E4C3F),
    onPrimaryContainer = Color(0xFFCFE0CF),
    secondary = Color(0xFFCFA187),
    onSecondary = Color(0xFF342216),
    secondaryContainer = Color(0xFF4C3A2D),
    onSecondaryContainer = Color(0xFFEDD6C4),
    tertiary = Color(0xFFB3A6BC),
    tertiaryContainer = Color(0xFF453D4C),
    onTertiaryContainer = Color(0xFFE0D7E6),
)

// 蒂芙尼蓝：知更鸟蓝 + 暖沙 + 雾蓝
val TiffanyDay = accentsDay(180f, 25f, 210f)
val TiffanyNight = accentsNight(180f, 25f, 210f)

// 海运蓝：藏蓝 + 钢青 + 黄铜
val OceanDay = accentsDay(213f, 190f, 42f)
val OceanNight = accentsNight(213f, 190f, 42f)

// 森林绿：松绿 + 橄榄 + 树皮
val ForestDay = accentsDay(152f, 88f, 28f)
val ForestNight = accentsNight(152f, 88f, 28f)
