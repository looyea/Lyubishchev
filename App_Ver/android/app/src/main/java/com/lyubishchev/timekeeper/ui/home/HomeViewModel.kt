package com.lyubishchev.timekeeper.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.data.EventMinutes
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 雷达图按哪一类事件铺开 / which category the radar axes come from */
enum class RadarDimension { CLASS_I, CLASS_II }

/** 雷达图比的是今天/昨天、本周/上周还是本月/上月 */
enum class RadarPeriod { TODAY, WEEK, MONTH }

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
 * Home state: today/week/month totals plus the radar model for the current toggles.
 */
data class HomeState(
    val dateText: String = TimeRules.todayText(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val monthMinutes: Int = 0,
    val dimension: RadarDimension = RadarDimension.CLASS_I,
    val period: RadarPeriod = RadarPeriod.TODAY,
    val radar: RadarModel = RadarModel(),
)

private data class RadarSelection(
    val dimension: RadarDimension = RadarDimension.CLASS_I,
    val period: RadarPeriod = RadarPeriod.TODAY,
)

private data class Breakdowns(
    val today: List<EventMinutes>,
    val yesterday: List<EventMinutes>,
    val week: List<EventMinutes>,
    val lastWeek: List<EventMinutes>,
    val month: List<EventMinutes>,
    val lastMonth: List<EventMinutes>,
)

/** combine 最多并 5 个流，本月先行合到这一层，上月单独再合一次 */
private data class FiveSets(
    val today: List<EventMinutes>,
    val yesterday: List<EventMinutes>,
    val week: List<EventMinutes>,
    val lastWeek: List<EventMinutes>,
    val month: List<EventMinutes>,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TimekeeperApp).repository

    /** ViewModel 跨 tab 存活，日期必须在运行时取，否则过午夜还查旧的一天 */
    private val currentDate = MutableStateFlow(TimeRules.todayText())

    private val selection = MutableStateFlow(RadarSelection())

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(60_000)
                refreshToday()
            }
        }
    }

    /** 每次重新进入概览页调用：立刻对齐到真实的今天 */
    fun refreshToday() {
        val real = TimeRules.todayText()
        if (real != currentDate.value) currentDate.value = real
    }

    fun selectDimension(dimension: RadarDimension) = selection.update { it.copy(dimension = dimension) }

    fun selectPeriod(period: RadarPeriod) = selection.update { it.copy(period = period) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeState> = currentDate
        .flatMapLatest { today ->
            val day = java.time.LocalDate.parse(today, TimeRules.DATE)
            val monthFrom = day.withDayOfMonth(1)
            combine(
                repository.breakdownOfDate(today),
                repository.breakdownOfDate(TimeRules.dateMinusDays(today, 1)),
                repository.breakdownOfWeek(today),
                repository.breakdownOfWeek(TimeRules.dateMinusDays(today, 7)),
                repository.breakdownOfRange(
                    monthFrom.format(TimeRules.DATE),
                    day.withDayOfMonth(day.lengthOfMonth()).format(TimeRules.DATE),
                ),
            ) { t, y, w, lw, m -> FiveSets(t, y, w, lw, m) }
                .combine(
                    repository.breakdownOfRange(
                        monthFrom.minusMonths(1).format(TimeRules.DATE),
                        monthFrom.minusDays(1).format(TimeRules.DATE),
                    ),
                ) { s, lm ->
                    today to Breakdowns(s.today, s.yesterday, s.week, s.lastWeek, s.month, lm)
                }
        }
        .combine(selection) { (today, breakdowns), chosen ->
            buildState(today, breakdowns, chosen)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(
                currentDate.value,
                Breakdowns(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
                selection.value,
            ),
        )

    private fun buildState(today: String, b: Breakdowns, chosen: RadarSelection): HomeState {
        val category = CategoryStore.nameAt(if (chosen.dimension == RadarDimension.CLASS_II) 1 else 0)
        val (currentRows, previousRows) = when (chosen.period) {
            RadarPeriod.TODAY -> b.today to b.yesterday
            RadarPeriod.WEEK -> b.week to b.lastWeek
            RadarPeriod.MONTH -> b.month to b.lastMonth
        }
        val current = currentRows.filter { it.category == category }
        val previous = previousRows.filter { it.category == category }
        // 轴跟着数据库走：当期有记录的事件各占一轴（删了/改了名，只要区间内有账就还在）。
        // 雷达至少要 3 根轴才画得出来，不够就用设置里的事件补足（补进来的显示 0）。
        val recordAxes = current.map { it.event }
            .ifEmpty { previous.map { it.event } }
            .distinct()
        val padded = if (recordAxes.size >= 3) recordAxes else
            recordAxes + CategoryStore.eventsFor(category).filterNot { it in recordAxes }
        val axes = padded.distinct().sortedWith(compareBy({ TimeRules.eventOrder(category, it) }, { it }))
        val currentValues = axes.map { e -> current.sumOf { if (it.event == e) it.minutes else 0 } }
        val previousValues = axes.map { e -> previous.sumOf { if (it.event == e) it.minutes else 0 } }
        return HomeState(
            dateText = today,
            todayMinutes = b.today.sumOf { it.minutes },
            weekMinutes = b.week.sumOf { it.minutes },
            monthMinutes = b.month.sumOf { it.minutes },
            dimension = chosen.dimension,
            period = chosen.period,
            radar = RadarModel(
                axes = axes,
                current = RadarSeries(currentValues),
                previous = RadarSeries(previousValues),
                scaleMax = TimeRules.radarScale((currentValues + previousValues).maxOrNull() ?: 0),
            ),
        )
    }
}
