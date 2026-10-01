package com.lyubishchev.timekeeper.domain

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

/**
 * 领域规则：分类 / 事件定义与时长、周数计算，与 Web 版保持一致。
 * Domain rules shared with the web client: categories, events, duration, ISO week.
 */
object TimeRules {

    const val CATEGORY_L1 = "I类时间"
    const val CATEGORY_L2 = "II类时间"

    /** 一级分类名，来自可维护的分类树 / live list of class names */
    val CATEGORIES: List<String> get() = CategoryStore.categoryNames()

    /** I类事件：维持生存与成长的时间 / class-I: sustaining & growth */
    val L1_EVENTS: List<String> get() = CategoryStore.eventsFor(CATEGORY_L1)

    /** II类事件：为时间本身服务的时间 / class-II: upkeep of the system */
    val L2_EVENTS: List<String> get() = CategoryStore.eventsFor(CATEGORY_L2)

    fun eventsFor(category: String): List<String> = CategoryStore.eventsFor(category)

    /** 事件在所属类别里的固定次序，未知事件排最后 / stable display order of an event */
    fun eventOrder(category: String, event: String): Int =
        CategoryStore.eventOrder(category, event)

    /** 记录页用罗马数字代替类别名 / roman badge for a category */
    fun roman(category: String): String = CategoryStore.roman(category)

    val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 分钟数 = 结束 - 开始；结束早于开始视为跨天。
     * Minutes between start and end; an earlier end time is treated as crossing midnight.
     */
    fun durationMinutes(startTime: String, endTime: String): Int {
        val start = LocalTime.parse(startTime, TIME)
        val end = LocalTime.parse(endTime, TIME)
        var minutes = Duration.between(start, end).toMinutes()
        if (minutes <= 0) minutes += 24 * 60
        return minutes.toInt()
    }

    /** "1小时30分钟" 式可读时长 / human readable duration */
    fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}分钟"
            m == 0 -> "${h}小时"
            else -> "${h}小时${m}分钟"
        }
    }

    /** "3.5h" 式十进制小时，报表用 / decimal hours for reports */
    fun formatHours(minutes: Int): String =
        String.format("%.1f", minutes / 60.0)

    fun isoWeek(date: LocalDate): Int =
        date.get(WeekFields.ISO.weekOfWeekBasedYear())

    fun isoWeek(dateText: String): Int = isoWeek(LocalDate.parse(dateText, DATE))

    fun year(dateText: String): Int = LocalDate.parse(dateText, DATE).year

    fun todayText(): String = LocalDate.now().format(DATE)

    /** 往前推若干天，用于"昨天/上周"这类对比期 */
    fun dateMinusDays(dateText: String, days: Long): String =
        LocalDate.parse(dateText, DATE).minusDays(days).format(DATE)

    /** 该日期所在 ISO 周的周一 / Monday of the ISO week containing the date */
    fun mondayOf(dateText: String): String =
        LocalDate.parse(dateText, DATE).with(java.time.DayOfWeek.MONDAY).format(DATE)

    /**
     * 雷达图刻度：取刚好盖住最大值的档位，全 0 时给 1 小时，避免图形塌成一个点。
     * Radar axis scale — the smallest ring value that still contains the data.
     */
    fun radarScale(maxMinutes: Int): Int {
        if (maxMinutes <= 0) return 60
        RADAR_SCALES.firstOrNull { it >= maxMinutes }?.let { return it }
        return ((maxMinutes + 359) / 360) * 360
    }

    private val RADAR_SCALES = intArrayOf(30, 60, 90, 120, 180, 240, 360, 480, 600, 720, 960, 1200, 1440)

    /** 当前时刻向下取到 5 分钟，与 Web 版时间步长一致 */
    fun now(): String {
        val now = LocalTime.now()
        return now.withMinute(now.minute / 5 * 5).withSecond(0).withNano(0).format(TIME)
    }

    /** 从某时刻累加分钟数，跨零点自动回绕 / add minutes, wrapping past midnight */
    fun plusMinutes(startTime: String, minutes: Int): String =
        LocalTime.parse(startTime, TIME).plusMinutes(minutes.toLong()).format(TIME)
}
