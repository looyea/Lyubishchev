package com.lyubishchev.timekeeper.ui.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.data.EventMinutes
import com.lyubishchev.timekeeper.data.TimeLogEntity
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** 记录页的四种查看粒度 / the four viewing granularities */
enum class LogMode { DAY, WEEK, MONTH, YEAR }

/** 聚合后的一行：某分类下某事件的总时长 */
data class StatRow(
    val roman: String,
    val category: String,
    val event: String,
    val minutes: Int,
)

/** 一周的汇总块，手风琴的一节 */
data class WeekBlock(
    val year: Int,
    val week: Int,
    val total: Int,
    val cat1: Int,
    val cat2: Int,
    val rows: List<StatRow>,
)

/** 一月的汇总块 */
data class MonthBlock(
    val month: Int,
    val total: Int,
    val cat1: Int,
    val cat2: Int,
    val rows: List<StatRow>,
)

/**
 * 记录页状态：只读展示，写入仍走"记一笔"。
 * Read-only log state for whichever mode + anchor date is selected.
 */
data class LogState(
    val mode: LogMode = LogMode.DAY,
    val anchor: String = TimeRules.todayText(),
    val dayEntries: List<TimeLogEntity> = emptyList(),
    val weeks: List<WeekBlock> = emptyList(),
    val months: List<MonthBlock> = emptyList(),
    val yearCat1: Int = 0,
    val yearCat2: Int = 0,
    val yearRows: List<StatRow> = emptyList(),
    val expanded: Set<String> = emptySet(),
)

private data class LogSelection(
    val mode: LogMode = LogMode.DAY,
    val anchor: String = TimeRules.todayText(),
)

private sealed interface Loaded {
    data class Day(val entries: List<TimeLogEntity>) : Loaded
    data class Weeks(val blocks: List<WeekBlock>) : Loaded
    data class Months(val blocks: List<MonthBlock>) : Loaded
    data class Year(val cat1: Int, val cat2: Int, val rows: List<StatRow>) : Loaded
}

class LogViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as TimekeeperApp).repository

    private val selection = MutableStateFlow(LogSelection())
    private val expanded = MutableStateFlow(emptySet<String>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LogState> = selection
        .flatMapLatest { sel -> flowFor(sel).map { sel to it } }
        .combine(expanded) { (sel, data), keys -> buildState(sel, data, keys) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            buildState(selection.value, null, emptySet()),
        )

    fun selectMode(mode: LogMode) = selection.update { it.copy(mode = mode) }

    fun selectAnchor(date: String) = selection.update { it.copy(anchor = date) }

    fun toggle(key: String) =
        expanded.update { if (key in it) it - key else it + key }

    /** 按当前模式取对应的数据流；周/月/年取的是聚合结果，不是原始行 */
    private fun flowFor(sel: LogSelection): Flow<Loaded> = when (sel.mode) {
        LogMode.DAY -> repository.entriesOfDate(sel.anchor)
            .map { Loaded.Day(it.reversed()) }

        LogMode.WEEK -> {
            val thisMonday = TimeRules.mondayOf(sel.anchor)
            val mondays = listOf(
                thisMonday,
                TimeRules.dateMinusDays(thisMonday, 7),
                TimeRules.dateMinusDays(thisMonday, 14),
            )
            val flows = mondays.map {
                repository.breakdownOfWeekBy(TimeRules.year(it), TimeRules.isoWeek(it))
            }
            combine(flows[0], flows[1], flows[2]) { a, b, c ->
                Loaded.Weeks(listOf(a, b, c).mapIndexed { i, rows -> weekBlock(mondays[i], rows) })
            }
        }

        LogMode.MONTH -> {
            val year = TimeRules.year(sel.anchor)
            repository.breakdownOfMonthYear(year).map { rows ->
                Loaded.Months(
                    rows.groupBy { it.month }.entries
                        .sortedByDescending { it.key }
                        .map { (month, group) ->
                            val stats = group.map {
                                StatRow(TimeRules.roman(it.category), it.category, it.event, it.minutes)
                            }.toSortedStatRows()
                            MonthBlock(
                                month = month,
                                total = group.sumOf { it.minutes },
                                cat1 = stats.cat1Minutes(),
                                cat2 = stats.cat2Minutes(),
                                rows = stats,
                            )
                        }
                )
            }
        }

        LogMode.YEAR -> {
            val year = TimeRules.year(sel.anchor)
            repository.breakdownOfRange("$year-01-01", "$year-12-31").map { rows ->
                val stats = rows.toStatRows()
                Loaded.Year(stats.cat1Minutes(), stats.cat2Minutes(), stats)
            }
        }
    }

    private fun weekBlock(monday: String, rows: List<EventMinutes>): WeekBlock {
        val stats = rows.toStatRows()
        return WeekBlock(
            year = TimeRules.year(monday),
            week = TimeRules.isoWeek(monday),
            total = rows.sumOf { it.minutes },
            cat1 = stats.cat1Minutes(),
            cat2 = stats.cat2Minutes(),
            rows = stats,
        )
    }

    private fun buildState(sel: LogSelection, data: Loaded?, keys: Set<String>): LogState =
        when (data) {
            is Loaded.Day -> LogState(sel.mode, sel.anchor, dayEntries = data.entries, expanded = keys)
            is Loaded.Weeks -> LogState(sel.mode, sel.anchor, weeks = data.blocks, expanded = keys)
            is Loaded.Months -> LogState(sel.mode, sel.anchor, months = data.blocks, expanded = keys)
            is Loaded.Year -> LogState(
                mode = sel.mode, anchor = sel.anchor,
                yearCat1 = data.cat1, yearCat2 = data.cat2, yearRows = data.rows,
                expanded = keys,
            )
            null -> LogState(mode = sel.mode, anchor = sel.anchor, expanded = keys)
        }
}

private fun List<EventMinutes>.toStatRows(): List<StatRow> =
    map { StatRow(TimeRules.roman(it.category), it.category, it.event, it.minutes) }
        .toSortedStatRows()

private fun List<StatRow>.toSortedStatRows(): List<StatRow> =
    sortedWith(
        compareBy<StatRow> { if (it.category == CategoryStore.nameAt(0)) 0 else 1 }
            .thenBy { TimeRules.eventOrder(it.category, it.event) }
            .thenByDescending { it.minutes },
    )

private fun List<StatRow>.cat1Minutes(): Int =
    filter { it.category == CategoryStore.nameAt(0) }.sumOf { it.minutes }

private fun List<StatRow>.cat2Minutes(): Int =
    filter { it.category == CategoryStore.nameAt(1) }.sumOf { it.minutes }
