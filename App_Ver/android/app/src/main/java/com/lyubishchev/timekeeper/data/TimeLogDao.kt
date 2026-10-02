package com.lyubishchev.timekeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * time_logs 的读写接口，Room 在编译期生成实现。
 * Compile-time generated SQLite access for time_logs.
 */
@Dao
interface TimeLogDao {

    @Insert
    suspend fun insert(entry: TimeLogEntity): Long

    @Insert
    suspend fun insertAll(entries: List<TimeLogEntity>): List<Long>

    @Update
    suspend fun update(entry: TimeLogEntity)

    @Delete
    suspend fun delete(entry: TimeLogEntity)

    /** 某天全部记录，按开始时间排序 / one day, ordered by start time */
    @Query(
        """
        SELECT * FROM time_logs
        WHERE date = :date
        ORDER BY start_time ASC, id ASC
        """
    )
    fun observeByDate(date: String): Flow<List<TimeLogEntity>>

    /** 某天合计分钟数 / total minutes logged on a day */
    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM time_logs WHERE date = :date")
    fun observeMinutesForDate(date: String): Flow<Int>

    /** 某 ISO 周合计分钟数 / total minutes logged in one ISO week */
    @Query(
        """
        SELECT COALESCE(SUM(duration_minutes), 0) FROM time_logs
        WHERE year = :year AND week_number = :week
        """
    )
    fun observeMinutesForWeek(year: Int, week: Int): Flow<Int>

    /**
     * 某天按 分类+事件 的分钟数分布，概览页雷达图用。
     * Per-category/per-event minutes for one day — the radar's raw material.
     */
    @Query(
        """
        SELECT category, event, COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        WHERE date = :date
        GROUP BY category, event
        """
    )
    fun observeBreakdownForDate(date: String): Flow<List<EventMinutes>>

    /** 某 ISO 周按 分类+事件 的分钟数分布 / the same breakdown for one ISO week */
    @Query(
        """
        SELECT category, event, COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        WHERE year = :year AND week_number = :week
        GROUP BY category, event
        """
    )
    fun observeBreakdownForWeek(year: Int, week: Int): Flow<List<EventMinutes>>

    /** 日期区间（闭区间，文本比较）按 分类+事件 的分布，周/年视图用 */
    @Query(
        """
        SELECT category, event, COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        WHERE date BETWEEN :fromDate AND :toDate
        GROUP BY category, event
        """
    )
    fun observeBreakdownForRange(fromDate: String, toDate: String): Flow<List<EventMinutes>>

    /** 区间内按自然月聚合，记录页月视图用 */
    @Query(
        """
        SELECT CAST(substr(date, 6, 2) AS INTEGER) AS month,
               category,
               event,
               COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        WHERE date BETWEEN :fromDate AND :toDate
        GROUP BY month, category, event
        """
    )
    fun observeBreakdownByMonth(fromDate: String, toDate: String): Flow<List<MonthEventMinutes>>

    /** 区间内按 (年份, ISO 周) 聚合，记录页周视图一次列出全年 */
    @Query(
        """
        SELECT year,
               week_number AS week,
               category,
               event,
               COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        WHERE date BETWEEN :fromDate AND :toDate
        GROUP BY year, week_number, category, event
        """
    )
    fun observeBreakdownByWeek(fromDate: String, toDate: String): Flow<List<WeekEventMinutes>>

    /** 全表按年份聚合，年视图因此天然只列出有记录的年份 */
    @Query(
        """
        SELECT year,
               category,
               event,
               COALESCE(SUM(duration_minutes), 0) AS minutes
        FROM time_logs
        GROUP BY year, category, event
        """
    )
    fun observeBreakdownByYear(): Flow<List<YearEventMinutes>>

    /** 最近若干条，用于列表页 / most recent rows for the log list */
    @Query("SELECT * FROM time_logs ORDER BY date DESC, start_time DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TimeLogEntity>>

    /** 区间内逐条记录，按日期+开始时间升序，导出用（一次性读取） */
    @Query(
        """
        SELECT * FROM time_logs
        WHERE date BETWEEN :fromDate AND :toDate
        ORDER BY date ASC, start_time ASC, id ASC
        """
    )
    suspend fun entriesInRange(fromDate: String, toDate: String): List<TimeLogEntity>

    /** 全表记录，导入去重时用 / whole table, for import dedup */
    @Query("SELECT * FROM time_logs ORDER BY date ASC, start_time ASC, id ASC")
    suspend fun entriesAll(): List<TimeLogEntity>

    /** 当天"结束最晚"的那条记录的结束时间，用来自动承接开始时间 */
    @Query("SELECT MAX(end_time) FROM time_logs WHERE date = :date")
    suspend fun lastEndTime(date: String): String?

    @Query("SELECT COUNT(*) FROM time_logs")
    suspend fun count(): Int

    /** 全表累计分钟数，设置页统计卡用 / whole-table total minutes */
    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM time_logs")
    suspend fun totalMinutes(): Int
}

/** 一条 GROUP BY 结果，不是表，只给概览页聚合用。 */
data class EventMinutes(
    val category: String,
    val event: String,
    val minutes: Int,
)

/** 月视图的 GROUP BY 结果：自然月 + 分类 + 事件。 */
data class MonthEventMinutes(
    val month: Int,
    val category: String,
    val event: String,
    val minutes: Int,
)

/** 周视图的 GROUP BY 结果：年份 + ISO 周 + 分类 + 事件。 */
data class WeekEventMinutes(
    val year: Int,
    val week: Int,
    val category: String,
    val event: String,
    val minutes: Int,
)

/** 年视图的 GROUP BY 结果：年份 + 分类 + 事件。 */
data class YearEventMinutes(
    val year: Int,
    val category: String,
    val event: String,
    val minutes: Int,
)
