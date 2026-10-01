package com.lyubishchev.timekeeper.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.data.EventMinutes
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** 雷达图按哪一类事件铺开 / which category the radar axes come from */
enum class RadarDimension { CLASS_I, CLASS_II }

/** 雷达图比的是"今天 vs 昨天"还是"本周 vs 上周" */
enum class RadarPeriod { TODAY, WEEK }

/** 雷达图里的一组多边形数据，values 与 axes 一一对应 */
data class RadarSeries(val values: List<Int>)

data class RadarModel(
    val axes: List<String> = emptyList(),
    val current: RadarSeries = RadarSeries(emptyList()),
    val previous: RadarSeries = RadarSeries(emptyList()),
    val scaleMax: Int = 60,
)

/**
 * 概览页状态。
 * Home state: headline totals plus the radar model for the current toggles.
 */
data class HomeState(
    val dateText: String = TimeRules.todayText(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val dimension: RadarDimension = RadarDimension.CLASS_I,
    val period: RadarPeriod = RadarPeriod.TODAY,
    val radar: RadarModel = RadarModel(),
    /** 所选类别在当期/对比期的合计，图例用 */
    val categoryMinutesCurrent: Int = 0,
    val categoryMinutesPrevious: Int = 0,
) {
    /** 卡片顶部那个大数字：当期一共记了多久（不分类别） */
    val headlineMinutes: Int get() = if (period == RadarPeriod.TODAY) todayMinutes else weekMinutes
}

private data class RadarSelection(
    val dimension: RadarDimension = RadarDimension.CLASS_I,
    val period: RadarPeriod = RadarPeriod.TODAY,
)

private data class Breakdowns(
    val today: List<EventMinutes>,
    val yesterday: List<EventMinutes>,
    val week: List<EventMinutes>,
    val lastWeek: List<EventMinutes>,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TimekeeperApp).repository

    private val today = TimeRules.todayText()
    private val yesterday = TimeRules.dateMinusDays(today, 1)
    /** 上周用"往前 7 天"定位，跨年时 year+week_number 会一起落到上一年 */
    private val lastWeekAnchor = TimeRules.dateMinusDays(today, 7)

    private val selection = MutableStateFlow(RadarSelection())

    fun selectDimension(dimension: RadarDimension) = selection.update { it.copy(dimension = dimension) }

    fun selectPeriod(period: RadarPeriod) = selection.update { it.copy(period = period) }

    val state: StateFlow<HomeState> = combine(
        repository.breakdownOfDate(today),
        repository.breakdownOfDate(yesterday),
        repository.breakdownOfWeek(today),
        repository.breakdownOfWeek(lastWeekAnchor),
    ) { todayRows, yesterdayRows, weekRows, lastWeekRows ->
        Breakdowns(todayRows, yesterdayRows, weekRows, lastWeekRows)
    }.combine(selection) { breakdowns, chosen ->
        buildState(breakdowns, chosen)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = buildState(Breakdowns(emptyList(), emptyList(), emptyList(), emptyList()), selection.value),
    )

    private fun buildState(b: Breakdowns, chosen: RadarSelection): HomeState {
        val category = CategoryStore.nameAt(if (chosen.dimension == RadarDimension.CLASS_II) 1 else 0)
        val axes = CategoryStore.eventsFor(category)
        val (currentRows, previousRows) = when (chosen.period) {
            RadarPeriod.TODAY -> b.today to b.yesterday
            RadarPeriod.WEEK -> b.week to b.lastWeek
        }
        val current = axes.map { event -> currentRows.minutesOf(category, event) }
        val previous = axes.map { event -> previousRows.minutesOf(category, event) }
        return HomeState(
            dateText = today,
            todayMinutes = b.today.sumOf { it.minutes },
            weekMinutes = b.week.sumOf { it.minutes },
            dimension = chosen.dimension,
            period = chosen.period,
            radar = RadarModel(
                axes = axes,
                current = RadarSeries(current),
                previous = RadarSeries(previous),
                scaleMax = TimeRules.radarScale((current + previous).maxOrNull() ?: 0),
            ),
            categoryMinutesCurrent = current.sum(),
            categoryMinutesPrevious = previous.sum(),
        )
    }

    private fun List<EventMinutes>.minutesOf(category: String, event: String): Int =
        filter { it.category == category && it.event == event }.sumOf { it.minutes }
}
