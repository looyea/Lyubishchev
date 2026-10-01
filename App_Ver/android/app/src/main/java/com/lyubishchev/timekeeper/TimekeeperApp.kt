package com.lyubishchev.timekeeper

import android.app.Application
import com.lyubishchev.timekeeper.data.AppDatabase
import com.lyubishchev.timekeeper.data.TimeLogRepository
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.ui.theme.ThemePrefs

/**
 * 持有数据库与仓库，供 ViewModel 取用。
 * Owns the database and repository so ViewModels stay trivial to construct.
 */
class TimekeeperApp : Application() {

    val repository: TimeLogRepository by lazy {
        TimeLogRepository(AppDatabase.get(this).timeLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        CategoryStore.init(this)
        ThemePrefs.init(this)
    }
}
