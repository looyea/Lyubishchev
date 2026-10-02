package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.ui.home.HomeState
import com.lyubishchev.timekeeper.ui.home.HomeViewModel
import com.lyubishchev.timekeeper.ui.home.RadarDimension
import com.lyubishchev.timekeeper.ui.home.RadarPeriod
import com.lyubishchev.timekeeper.ui.theme.NumberSerif
import com.lyubishchev.timekeeper.ui.widget.ChoiceRow
import com.lyubishchev.timekeeper.ui.widget.RadarChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 概览页：日期 + 今日/本周/本月三条累计，下方一张可切换的雷达图卡片。
 * Home: the date, today/week/month totals, then one radar card that toggles
 * between class-I/class-II events and between today-vs-yesterday / thisweek-vs-lastweek /
 * thismonth-vs-lastmonth.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshToday() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = chineseDate(state.dateText),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        // 今日 / 本周 / 本月 三条合计并排一行，与雷达图的时间档一一对应
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            StatBlock(
                label = stringResource(R.string.home_stat_today),
                value = TimeRules.formatMinutes(state.todayMinutes),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = stringResource(R.string.home_stat_week),
                value = TimeRules.formatMinutes(state.weekMinutes),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = stringResource(R.string.home_stat_month),
                value = TimeRules.formatMinutes(state.monthMinutes),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        RadarCard(
            state = state,
            onDimension = viewModel::selectDimension,
            onPeriod = viewModel::selectPeriod,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** 日期下方并排的合计块：上小标签、下主色大号衬线数值。 */
@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = NumberSerif,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** 雷达图卡片：图形 + 居中图例 + 两组切换；总量看上方统计块，不在卡片里重复。 */
@Composable
private fun RadarCard(
    state: HomeState,
    onDimension: (RadarDimension) -> Unit,
    onPeriod: (RadarPeriod) -> Unit,
) {
    val (currentLabelRes, previousLabelRes) = when (state.period) {
        RadarPeriod.TODAY -> R.string.home_series_today to R.string.home_series_yesterday
        RadarPeriod.WEEK -> R.string.home_series_week to R.string.home_series_last_week
        RadarPeriod.MONTH -> R.string.home_series_month to R.string.home_series_last_month
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadarChart(
            axes = state.radar.axes,
            current = state.radar.current.values,
            previous = state.radar.previous.values,
            scaleMax = state.radar.scaleMax,
            modifier = Modifier
                .fillMaxWidth()
                .height(272.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))

        RadarLegend(
            currentLabel = stringResource(currentLabelRes),
            previousLabel = stringResource(previousLabelRes),
        )

        Spacer(modifier = Modifier.height(18.dp))
        // 一类/二类标签永远跟着分类设置走（固定两类，名字可在"时间分类"里改）
        ChoiceRow(
            options = RadarDimension.entries.toList(),
            selected = state.dimension,
            labelOf = { CategoryStore.nameAt(if (it == RadarDimension.CLASS_II) 1 else 0) },
            onSelect = onDimension,
        )
        Spacer(modifier = Modifier.height(10.dp))
        ChoiceRow(
            options = RadarPeriod.entries.toList(),
            selected = state.period,
            labelOf = { stringResource(periodLabel(it)) },
            onSelect = onPeriod,
        )
    }
}

@Composable
private fun RadarLegend(currentLabel: String, previousLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(MaterialTheme.colorScheme.primary)
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 6.dp),
        )
        Spacer(modifier = Modifier.width(18.dp))
        LegendDot(MaterialTheme.colorScheme.secondary)
        Text(
            text = previousLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun LegendDot(color: Color) {
    Spacer(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@StringRes
private fun periodLabel(period: RadarPeriod): Int = when (period) {
    RadarPeriod.TODAY -> R.string.home_period_today
    RadarPeriod.WEEK -> R.string.home_period_week
    RadarPeriod.MONTH -> R.string.home_period_month
}

private val CHINESE_DATE: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日 EEEE", Locale.CHINA)

private fun chineseDate(isoDate: String): String =
    LocalDate.parse(isoDate, TimeRules.DATE).format(CHINESE_DATE)
