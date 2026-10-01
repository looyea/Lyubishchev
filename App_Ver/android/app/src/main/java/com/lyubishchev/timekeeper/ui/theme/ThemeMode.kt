package com.lyubishchev.timekeeper.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 主题三档：跟随系统 / 浅色 / 深色 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * 主题选择的极简存储：内存态直接驱动重组，落盘用 SharedPreferences。
 * No DataStore dependency: a Compose state backed by SharedPreferences.
 */
object ThemePrefs {

    private const val FILE = "theme_prefs"
    private const val KEY_MODE = "theme_mode"

    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        mode = prefs(context).getString(KEY_MODE, null)
            ?.let { stored -> runCatching { ThemeMode.valueOf(stored) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    fun set(context: Context, chosen: ThemeMode) {
        mode = chosen
        prefs(context).edit().putString(KEY_MODE, chosen.name).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
