package com.lyubishchev.timekeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 本地数据库单例。纯离线应用，数据只存手机本地。
 * Local-only database; the app never talks to a server.
 */
@Database(
    entities = [TimeLogEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeLogDao(): TimeLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timekeeper.db",
                ).build().also { instance = it }
            }
    }
}
