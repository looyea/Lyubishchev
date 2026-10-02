package com.lyubishchev.timekeeper

import android.app.Application
import com.lyubishchev.timekeeper.data.AppDatabase
import com.lyubishchev.timekeeper.data.TimeLogRepository
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.ReportTemplateStore
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.theme.ThemePrefs

/**
 * 持有数据库与仓库，供 ViewModel 取用。
 * Owns the database and repository so ViewModels stay trivial to construct.
 */
class TimekeeperApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }

    val repository: TimeLogRepository by lazy {
        TimeLogRepository(database.timeLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        // 语言必须最先定下来：后面所有非 Compose 文案都从 AppLocale.context 取
        AppLocale.init(this)
        CategoryStore.init(this)
        ThemePrefs.init(this)
        ReportTemplateStore.init(this)
    }
}
