package com.lyubishchev.timekeeper.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 一条时间记录，落在 SQLite 的 time_logs 表。
 * One Lyubishchev time-log row in SQLite.
 *
 * date / start_time / end_time 存文本（YYYY-MM-DD、HH:mm），与 Web 版一致，
 * 便于按字符串直接排序与等值查询；派生列 week_number / year 写入时算好，
 * 让周、年统计走索引而不必在 SQL 里做日期函数。
 *
 * Text columns match the web client so ordering and equality lookups stay cheap;
 * week_number / year are denormalised at write time so reports hit indexes instead
 * of computing dates in SQL.
 */
@Entity(
    tableName = "time_logs",
    indices = [
        Index("date"),
        Index(value = ["year", "week_number"]),
    ],
)
data class TimeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "start_time")
    val startTime: String,

    @ColumnInfo(name = "end_time")
    val endTime: String,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "event")
    val event: String,

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "week_number")
    val weekNumber: Int,

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
