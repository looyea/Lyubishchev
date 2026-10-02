package com.lyubishchev.timekeeper.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 颜色模式三档：跟随系统 / 浅色 / 深色 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 主题：整套控件配色的换色盘，中性底色始终是莫兰迪。 */
enum class ThemePalette(val day: Accents, val night: Accents) {
    MORANDI(MorandiDay, MorandiNight),
    TIFFANY(TiffanyDay, TiffanyNight),
    OCEAN(OceanDay, OceanNight),
    FOREST(ForestDay, ForestNight),
    CRIMSON(CrimsonDay, CrimsonNight),
    AMBER(AmberDay, AmberNight),
    ROSE(RoseDay, RoseNight),
    GRAPHITE(GraphiteDay, GraphiteNight),
    OBSIDIAN(ObsidianDay, ObsidianNight),
    SOLARIZED(SolarizedDay, SolarizedNight),
}

/**
 * 主题选择的极简存储：内存态直接驱动重组，落盘用 SharedPreferences。
 * No DataStore dependency: a Compose state backed by SharedPreferences.
 */
object ThemePrefs {

    private const val FILE = "theme_prefs"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_PALETTE = "theme_palette"

    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    var palette by mutableStateOf(ThemePalette.MORANDI)
        private set

    fun init(context: Context) {
        mode = read(context, KEY_MODE, ThemeMode.values(), ThemeMode.SYSTEM)
        palette = read(context, KEY_PALETTE, ThemePalette.values(), ThemePalette.MORANDI)
    }

    fun set(context: Context, chosen: ThemeMode) = write(context, KEY_MODE, chosen) { mode = chosen }

    fun set(context: Context, chosen: ThemePalette) =
        write(context, KEY_PALETTE, chosen) { palette = chosen }

    private fun <T : Enum<T>> read(context: Context, key: String, all: Array<T>, fallback: T): T =
        prefs(context).getString(key, null)
            ?.let { stored -> all.firstOrNull { it.name == stored } }
            ?: fallback

    private fun <T : Enum<T>> write(context: Context, key: String, value: T, apply: () -> Unit) {
        apply()
        prefs(context).edit().putString(key, value.name).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
