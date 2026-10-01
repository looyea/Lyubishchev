package com.lyubishchev.timekeeper.ui

import androidx.annotation.StringRes
import com.lyubishchev.timekeeper.R

/**
 * 底部五个一级入口，中间是「记一笔」快捷入口（抖音式布局）。
 * Five top-level destinations; the center one is the quick "log now" action.
 */
enum class AppTab(@StringRes val labelRes: Int) {
    Home(R.string.tab_home),
    Log(R.string.tab_log),
    Add(R.string.tab_add),
    Report(R.string.tab_report),
    Mine(R.string.tab_mine),
}
