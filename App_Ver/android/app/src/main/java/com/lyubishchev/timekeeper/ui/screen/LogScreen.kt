package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.data.TimeLogEntity
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.log.LogMode
import com.lyubishchev.timekeeper.ui.log.LogState
import com.lyubishchev.timekeeper.ui.log.LogViewModel
import com.lyubishchev.timekeeper.ui.log.StatRow
import com.lyubishchev.timekeeper.ui.theme.NumberSerif
import com.lyubishchev.timekeeper.ui.widget.ChoiceRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 记录页：日/周/月/年四档只读浏览 + 日期选择。
 * Read-only browser over the logs with day/week/month/year granularities
 * and an anchor-date picker; writing stays in the "记一笔" tab.
 */
@Composable
fun LogScreen(
    viewModel: LogViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.alignWithToday() }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ChoiceRow(
                options = LogMode.entries.toList(),
                selected = state.mode,
                labelOf = { stringResource(modeLabel(it)) },
                onSelect = viewModel::selectMode,
                modifier = Modifier.weight(1f),
            )
            // 年视图直接铺开数据库里有记录的年份，没有"锚点"可选，所以按钮置灰但仍占位
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(
                onClick = { showPicker = true },
                enabled = state.mode != LogMode.YEAR,
            ) {
                Text(anchorLabel(state))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        when (state.mode) {
            LogMode.DAY -> DayList(state, viewModel::toggle)
            LogMode.WEEK -> WeekList(state, viewModel::toggle)
            LogMode.MONTH -> MonthList(state, viewModel::toggle)
            LogMode.YEAR -> YearList(state)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPicker) {
        AnchorDatePicker(
            anchor = state.anchor,
            mode = state.mode,
            onPick = viewModel::selectAnchor,
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun DayList(state: LogState, onToggle: (String) -> Unit) {
    if (state.dayEntries.isEmpty()) {
        EmptyHint()
        return
    }
    state.dayEntries.forEach { entry ->
        EntryAccordion(
            entry = entry,
            expanded = "e${entry.id}" in state.expanded,
            onToggle = { onToggle("e${entry.id}") },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 单条记录：折叠态给全 类别/事件/起止/时长；
 * 只有写了备注才允许展开，没备注的记录根本不用点。
 */
@Composable
private fun EntryAccordion(entry: TimeLogEntity, expanded: Boolean, onToggle: () -> Unit) {
    val hasNote = entry.note.isNotEmpty()
    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasNote) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RomanBadge(entry.category)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = entry.event,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${entry.startTime}—${entry.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = TimeRules.formatMinutes(entry.durationMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(visible = expanded && hasNote) {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailLine(stringResource(R.string.field_note), entry.note)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WeekList(state: LogState, onToggle: (String) -> Unit) {
    val today = TimeRules.todayText()
    val currentYear = TimeRules.year(today)
    val currentWeek = TimeRules.isoWeek(today)
    state.weeks.forEach { block ->
        val key = "w${block.year}-${block.week}"
        val currentSuffix =
            if (block.year == currentYear && block.week == currentWeek) {
                stringResource(R.string.log_week_current_suffix)
            } else {
                ""
            }
        PeriodAccordion(
            title = stringResource(R.string.log_week_title, block.week) + currentSuffix,
            total = block.total,
            expanded = key in state.expanded,
            onToggle = { onToggle(key) },
            cat1 = block.cat1,
            cat2 = block.cat2,
            rows = block.rows,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MonthList(state: LogState, onToggle: (String) -> Unit) {
    if (state.months.isEmpty()) {
        EmptyHint()
        return
    }
    state.months.forEach { block ->
        val key = "m${TimeRules.year(state.anchor)}-${block.month}"
        PeriodAccordion(
            title = stringResource(R.string.log_month_title, block.month),
            total = block.total,
            expanded = key in state.expanded,
            onToggle = { onToggle(key) },
            cat1 = block.cat1,
            cat2 = block.cat2,
            rows = block.rows,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** 周/月共用的一节手风琴：折叠=标题+总计，展开=一类/二类合计+逐事件。 */
@Composable
private fun PeriodAccordion(
    title: String,
    total: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    cat1: Int,
    cat2: Int,
    rows: List<StatRow>,
) {
    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.log_total, TimeRules.formatMinutes(total)),
                style = NumberSerif,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.log_cat_totals, TimeRules.formatMinutes(cat1), TimeRules.formatMinutes(cat2)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (rows.isEmpty()) {
                    Text(
                        text = stringResource(R.string.log_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    rows.forEach { StatRowLine(it) }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/** 年视图：只列出数据库里有记录的年份，一年一块直接铺开，不用日期锚点。 */
@Composable
private fun YearList(state: LogState) {
    if (state.years.isEmpty()) {
        EmptyHint()
        return
    }
    state.years.forEach { year ->
        SectionCard {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.log_year_title, year.year),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.log_total, TimeRules.formatMinutes(year.total)),
                        style = NumberSerif,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.log_cat_totals,
                        TimeRules.formatMinutes(year.cat1),
                        TimeRules.formatMinutes(year.cat2),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(6.dp))
                year.rows.forEach { StatRowLine(it) }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun StatRowLine(row: StatRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RomanBadge(row.category)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = row.event,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = TimeRules.formatMinutes(row.minutes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RomanBadge(category: String) {
    val isPrimary = category == CategoryStore.nameAt(0)
    Text(
        text = TimeRules.roman(category),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        modifier = Modifier.width(22.dp),
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
    ) { content() }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyHint() {
    Text(
        text = stringResource(R.string.log_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnchorDatePicker(
    anchor: String,
    mode: LogMode,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val anchorDate = LocalDate.parse(anchor, TimeRules.DATE)
    val today = LocalDate.parse(TimeRules.todayText(), TimeRules.DATE)
    // 周/月只在锚点那一年的 1 月 1 日到"当前"之间选：下周、下月这些还没到的日子选不了
    val lower = if (mode == LogMode.DAY) LocalDate.of(2000, 1, 1) else anchorDate.withDayOfYear(1)
    val upper = when {
        mode == LogMode.DAY -> today
        anchorDate.year < today.year -> anchorDate.withDayOfYear(anchorDate.lengthOfYear())
        else -> today
    }
    val initialMillis = anchorDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = BoundedDates(lower, upper),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    val picked = Instant.ofEpochMilli(millis)
                        .atZone(ZoneOffset.UTC).toLocalDate().format(TimeRules.DATE)
                    onPick(picked)
                }
                onDismiss()
            }) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

/** 只允许选 [start, end] 区间内的日子（含两端）：用来挡掉还没到来的周/月。 */
private class BoundedDates(
    private val start: LocalDate,
    private val end: LocalDate,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return !day.isBefore(start) && !day.isAfter(end)
    }

    override fun isSelectableYear(year: Int): Boolean = year in start.year..end.year
}

private fun anchorLabel(state: LogState): String {
    val date = LocalDate.parse(state.anchor, TimeRules.DATE)
    return when (state.mode) {
        LogMode.DAY -> date.format(AppLocale.dateFormatter(R.string.pattern_date_short))
        LogMode.WEEK -> AppLocale.str(R.string.log_week_title, TimeRules.isoWeek(state.anchor))
        LogMode.MONTH -> AppLocale.str(R.string.log_anchor_month, date.year, date.monthValue)
        LogMode.YEAR -> AppLocale.str(R.string.log_year_title, date.year)
    }
}

@StringRes
private fun modeLabel(mode: LogMode): Int = when (mode) {
    LogMode.DAY -> R.string.log_mode_day
    LogMode.WEEK -> R.string.log_mode_week
    LogMode.MONTH -> R.string.log_mode_month
    LogMode.YEAR -> R.string.log_mode_year
}
