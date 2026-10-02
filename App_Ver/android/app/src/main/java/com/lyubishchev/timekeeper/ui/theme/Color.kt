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

/** 低饱和色相轮：只给色相，明度和饱和度按深浅色模式定死，保证和莫兰迪中性层同气质。sat 用来把某套配色调得更艳或更灰。 */
private fun accentsDay(h1: Float, h2: Float, h3: Float, sat: Float = 1f) = Accents(
    primary = Color.hsl(h1, s(0.26f, sat), 0.40f),
    onPrimary = Color.hsl(h1, s(0.30f, sat), 0.96f),
    primaryContainer = Color.hsl(h1, s(0.24f, sat), 0.87f),
    onPrimaryContainer = Color.hsl(h1, s(0.30f, sat), 0.20f),
    secondary = Color.hsl(h2, s(0.30f, sat), 0.45f),
    onSecondary = Color.hsl(h2, s(0.32f, sat), 0.96f),
    secondaryContainer = Color.hsl(h2, s(0.30f, sat), 0.88f),
    onSecondaryContainer = Color.hsl(h2, s(0.34f, sat), 0.22f),
    tertiary = Color.hsl(h3, s(0.22f, sat), 0.44f),
    tertiaryContainer = Color.hsl(h3, s(0.24f, sat), 0.89f),
    onTertiaryContainer = Color.hsl(h3, s(0.26f, sat), 0.21f),
)

private fun accentsNight(h1: Float, h2: Float, h3: Float, sat: Float = 1f) = Accents(
    primary = Color.hsl(h1, s(0.24f, sat), 0.72f),
    onPrimary = Color.hsl(h1, s(0.30f, sat), 0.14f),
    primaryContainer = Color.hsl(h1, s(0.20f, sat), 0.30f),
    onPrimaryContainer = Color.hsl(h1, s(0.24f, sat), 0.87f),
    secondary = Color.hsl(h2, s(0.30f, sat), 0.68f),
    onSecondary = Color.hsl(h2, s(0.34f, sat), 0.14f),
    secondaryContainer = Color.hsl(h2, s(0.22f, sat), 0.32f),
    onSecondaryContainer = Color.hsl(h2, s(0.28f, sat), 0.86f),
    tertiary = Color.hsl(h3, s(0.20f, sat), 0.68f),
    tertiaryContainer = Color.hsl(h3, s(0.20f, sat), 0.32f),
    onTertiaryContainer = Color.hsl(h3, s(0.24f, sat), 0.87f),
)

private fun s(base: Float, boost: Float): Float = (base * boost).coerceAtMost(0.72f)

/**
 * 一套中性层：大底/卡片/浮起层/文字/次要文字/描边，随所属色盘染色。
 * v0.93 起每套主题自带配套的中性层，浅色是「带色相的浅」、深色是「带色相的深」，
 * 只有默认的莫兰迪保持原来的暖米白 / 炭灰。
 */
data class Neutrals(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val text: Color,
    val muted: Color,
    val outline: Color,
    val outlineVariant: Color,
)

/**
 * 浅色中性层生成器：hue 取该色盘的主色相，sat 沿用该盘已有的饱和乘数。
 * 高亮度的白底能容纳的色度很小（HSL 里 chroma = (1-|2L-1|)*S），所以浅色的饱和基数
 * 看着比深色大得多，实际染出来的深浅两套「色感」才一致。
 */
private fun neutralsDay(hue: Float, sat: Float = 1f) = Neutrals(
    background = Color.hsl(hue, ns(0.50f, sat), 0.952f),
    surface = Color.hsl(hue, ns(0.50f, sat), 0.972f),
    surfaceHigh = Color.hsl(hue, ns(0.30f, sat), 0.905f),
    text = Color.hsl(hue, ns(0.08f, sat), 0.185f),
    muted = Color.hsl(hue, ns(0.055f, sat), 0.400f),
    outline = Color.hsl(hue, ns(0.06f, sat), 0.575f),
    outlineVariant = Color.hsl(hue, ns(0.21f, sat), 0.868f),
)

/** 深色中性层生成器：同一主色相，深底染色天然比浅底显眼，饱和基数相应放小。 */
private fun neutralsNight(hue: Float, sat: Float = 1f) = Neutrals(
    background = Color.hsl(hue, ns(0.17f, sat), 0.095f),
    surface = Color.hsl(hue, ns(0.16f, sat), 0.135f),
    surfaceHigh = Color.hsl(hue, ns(0.15f, sat), 0.195f),
    text = Color.hsl(hue, ns(0.06f, sat), 0.925f),
    muted = Color.hsl(hue, ns(0.09f, sat), 0.635f),
    outline = Color.hsl(hue, ns(0.10f, sat), 0.425f),
    outlineVariant = Color.hsl(hue, ns(0.15f, sat), 0.215f),
)

/** 中性层的饱和上限：防止高 sat 乘数的色盘（朱砂红 / 琥珀黄 / 蔷薇粉）染到刺眼 */
private fun ns(base: Float, boost: Float): Float = (base * boost).coerceAtMost(0.78f)

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

// 朱砂红：砖红 + 陶土 + 赤金（红系拉高一点饱和，否则太像莫兰迪底色）
val CrimsonDay = accentsDay(355f, 18f, 40f, 1.45f)
val CrimsonNight = accentsNight(355f, 18f, 40f, 1.45f)

// 琥珀黄：缃黄 + 赭石 + 橄榄
val AmberDay = accentsDay(46f, 30f, 88f, 1.5f)
val AmberNight = accentsNight(46f, 30f, 88f, 1.5f)

// 蔷薇粉：玫红 + 珊瑚 + 兰紫
val RoseDay = accentsDay(338f, 355f, 300f, 1.4f)
val RoseNight = accentsNight(338f, 355f, 300f, 1.4f)

// 石墨灰：冷灰三档，几乎不带彩
val GraphiteDay = accentsDay(215f, 225f, 205f, 0.35f)
val GraphiteNight = accentsNight(215f, 225f, 205f, 0.35f)

// 曜石黑：浅色下压到近黑，深色下反相成浅灰，否则控件在底上看不见
val ObsidianDay = Accents(
    primary = Color(0xFF232326),
    onPrimary = Color(0xFFF7F5F0),
    primaryContainer = Color(0xFFDAD7D0),
    onPrimaryContainer = Color(0xFF17171A),
    secondary = Color(0xFF4C4C52),
    onSecondary = Color(0xFFF8F8F9),
    secondaryContainer = Color(0xFFE4E4E7),
    onSecondaryContainer = Color(0xFF22222A),
    tertiary = Color(0xFF74757C),
    tertiaryContainer = Color(0xFFEAEAEC),
    onTertiaryContainer = Color(0xFF2C2D33),
)

val ObsidianNight = Accents(
    primary = Color(0xFFE7E4DC),
    onPrimary = Color(0xFF1A1A1C),
    primaryContainer = Color(0xFF3A3A3E),
    onPrimaryContainer = Color(0xFFECEBE4),
    secondary = Color(0xFFBFC2C6),
    onSecondary = Color(0xFF1D1E21),
    secondaryContainer = Color(0xFF46484C),
    onSecondaryContainer = Color(0xFFDFE1E4),
    tertiary = Color(0xFF8E9095),
    tertiaryContainer = Color(0xFF4C4E52),
    onTertiaryContainer = Color(0xFFD6D7DA),
)

// Solarized：终端配色老熟人，天蓝 + 青 + 品红
val SolarizedDay = Accents(
    primary = Color(0xFF268BD2),
    onPrimary = Color(0xFFFDF6E3),
    primaryContainer = Color(0xFFCFE1F0),
    onPrimaryContainer = Color(0xFF073642),
    secondary = Color(0xFF2AA198),
    onSecondary = Color(0xFFFDF6E3),
    secondaryContainer = Color(0xFFC9E2DD),
    onSecondaryContainer = Color(0xFF073642),
    tertiary = Color(0xFFD33682),
    tertiaryContainer = Color(0xFFEFCEDE),
    onTertiaryContainer = Color(0xFF3F0E33),
)

val SolarizedNight = Accents(
    primary = Color(0xFF5EB3E8),
    onPrimary = Color(0xFF002B36),
    primaryContainer = Color(0xFF124B63),
    onPrimaryContainer = Color(0xFFBFE0F5),
    secondary = Color(0xFF52B6AC),
    onSecondary = Color(0xFF002B36),
    secondaryContainer = Color(0xFF124F4A),
    onSecondaryContainer = Color(0xFFC0E8E2),
    tertiary = Color(0xFFE06BB0),
    tertiaryContainer = Color(0xFF4A1F42),
    onTertiaryContainer = Color(0xFFF5CFE9),
)

// ── 每套主题配套的中性层（v0.93）：大底 / 卡片 / 浮起 / 文字 / 次要 / 描边 ──

// 莫兰迪是「无主题」基准，中性层就是上面那组全局常量原值，一字未改。
val MorandiDayNeutrals = Neutrals(
    background = DayBackground,
    surface = DaySurface,
    surfaceHigh = DaySurfaceHigh,
    text = DayText,
    muted = DayMuted,
    outline = DayOutline,
    outlineVariant = DayOutlineVariant,
)

val MorandiNightNeutrals = Neutrals(
    background = NightBackground,
    surface = NightSurface,
    surfaceHigh = NightSurfaceHigh,
    text = NightText,
    muted = NightMuted,
    outline = NightOutline,
    outlineVariant = NightOutlineVariant,
)

// 色相轮生成的七套：主色相与上面 Accents 用的 h1、sat 乘数保持一致
val TiffanyDayNeutrals = neutralsDay(180f)
val TiffanyNightNeutrals = neutralsNight(180f)

val OceanDayNeutrals = neutralsDay(213f)
val OceanNightNeutrals = neutralsNight(213f)

val ForestDayNeutrals = neutralsDay(152f)
val ForestNightNeutrals = neutralsNight(152f)

val CrimsonDayNeutrals = neutralsDay(355f, 1.45f)
val CrimsonNightNeutrals = neutralsNight(355f, 1.45f)

val AmberDayNeutrals = neutralsDay(46f, 1.5f)
val AmberNightNeutrals = neutralsNight(46f, 1.5f)

val RoseDayNeutrals = neutralsDay(338f, 1.4f)
val RoseNightNeutrals = neutralsNight(338f, 1.4f)

// 石墨灰 sat 只有 0.35，染色几乎看不出，正是「沉稳」要的效果
val GraphiteDayNeutrals = neutralsDay(215f, 0.35f)
val GraphiteNightNeutrals = neutralsNight(215f, 0.35f)

// 曜石黑：近中性冷灰，深色下刻意比莫兰迪更沉
val ObsidianDayNeutrals = Neutrals(
    background = Color(0xFFF5F4F2),
    surface = Color(0xFFFAFAF9),
    surfaceHigh = Color(0xFFEAE9E6),
    text = Color(0xFF1B1B1E),
    muted = Color(0xFF66666C),
    outline = Color(0xFF9A9AA0),
    outlineVariant = Color(0xFFDEDDD9),
)

val ObsidianNightNeutrals = Neutrals(
    background = Color(0xFF111113),
    surface = Color(0xFF191A1C),
    surfaceHigh = Color(0xFF232427),
    text = Color(0xFFECEBE6),
    muted = Color(0xFFA3A3A8),
    outline = Color(0xFF4A4A50),
    outlineVariant = Color(0xFF2C2D31),
)

// Solarized：官方 base 色，浅色羊皮纸 / 深蓝黑
val SolarizedDayNeutrals = Neutrals(
    background = Color(0xFFFBF3DE),
    surface = Color(0xFFFDF6E3),
    surfaceHigh = Color(0xFFEEE8D5),
    text = Color(0xFF073642),
    muted = Color(0xFF586E75),
    outline = Color(0xFF93A1A1),
    outlineVariant = Color(0xFFE5E0C8),
)

val SolarizedNightNeutrals = Neutrals(
    background = Color(0xFF002B36),
    surface = Color(0xFF073642),
    surfaceHigh = Color(0xFF0C4051),
    text = Color(0xFFEEE8D5),
    muted = Color(0xFF93A1A1),
    outline = Color(0xFF586E75),
    outlineVariant = Color(0xFF1B4A56),
)
