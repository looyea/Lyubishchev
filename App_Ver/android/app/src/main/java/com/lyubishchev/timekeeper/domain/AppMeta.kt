package com.lyubishchev.timekeeper.domain

import android.content.Context
import java.time.LocalDate

/** 首次运行那天记下来，启动屏据此算陪伴天数；只写一次，卸载重装才会重置。 */
object AppMeta {

    private const val FILE = "app_meta_prefs"
    private const val KEY_FIRST_DAY = "first_epoch_day"

    fun daysTogether(context: Context): Int {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val today = LocalDate.now().toEpochDay()
        val stored = prefs.getLong(KEY_FIRST_DAY, -1L)
        val first = if (stored > 0L) stored else {
            prefs.edit().putLong(KEY_FIRST_DAY, today).apply()
            today
        }
        return (today - first + 1).coerceAtLeast(1L).toInt()
    }
}
