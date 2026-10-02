package com.lyubishchev.timekeeper.data

import com.lyubishchev.timekeeper.domain.TimeRules

/**
 * 唯一的数据出入口：把用户填的日期/起止换算成入库字段。
 * Single entry point to storage; derives duration, ISO week and year on write.
 */
class TimeLogRepository(private val dao: TimeLogDao) {

    fun entriesOfDate(date: String) = dao.observeByDate(date)

    fun minutesOfDate(date: String) = dao.observeMinutesForDate(date)

    fun minutesOfWeek(date: String) =
        dao.observeMinutesForWeek(TimeRules.year(date), TimeRules.isoWeek(date))

    fun breakdownOfDate(date: String) = dao.observeBreakdownForDate(date)

    /** 与 date 同一 ISO 周的每一类/每个事件各花了多久 */
    fun breakdownOfWeek(date: String) =
        dao.observeBreakdownForWeek(TimeRules.year(date), TimeRules.isoWeek(date))

    fun breakdownOfWeekBy(year: Int, week: Int) = dao.observeBreakdownForWeek(year, week)

    fun breakdownOfRange(fromDate: String, toDate: String) =
        dao.observeBreakdownForRange(fromDate, toDate)

    /** 某一年 1-12 月的逐月分布 / per-month breakdown inside one calendar year */
    fun breakdownOfMonthYear(year: Int) =
        dao.observeBreakdownByMonth("$year-01-01", "$year-12-31")

    /** 某一年逐 ISO 周的分布 / per-ISO-week breakdown inside one calendar year */
    fun breakdownOfWeeksOfYear(fromDate: String, toDate: String) =
        dao.observeBreakdownByWeek(fromDate, toDate)

    /** 所有留有记录的年份 / every year that actually has rows */
    fun breakdownOfYears() = dao.observeBreakdownByYear()

    fun recent(limit: Int = 200) = dao.observeRecent(limit)

    suspend fun entriesInRange(fromDate: String, toDate: String) =
        dao.entriesInRange(fromDate, toDate)

    suspend fun entriesAll() = dao.entriesAll()

    suspend fun insertAll(entries: List<TimeLogEntity>) = dao.insertAll(entries)

    suspend fun lastEndTime(date: String): String? = dao.lastEndTime(date)

    suspend fun count(): Int = dao.count()

    /**
     * 保存一条记录，返回时长（分钟）供界面提示。
     * Persists one entry and reports its minutes back to the UI.
     */
    suspend fun add(
        date: String,
        startTime: String,
        endTime: String,
        category: String,
        event: String,
        note: String,
    ): Int {
        val minutes = TimeRules.durationMinutes(startTime, endTime)
        dao.insert(
            TimeLogEntity(
                date = date,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = minutes,
                category = category,
                event = event,
                note = note.trim(),
                weekNumber = TimeRules.isoWeek(date),
                year = TimeRules.year(date),
            )
        )
        return minutes
    }

    suspend fun update(entry: TimeLogEntity) = dao.update(entry)

    suspend fun delete(entry: TimeLogEntity) = dao.delete(entry)
}
