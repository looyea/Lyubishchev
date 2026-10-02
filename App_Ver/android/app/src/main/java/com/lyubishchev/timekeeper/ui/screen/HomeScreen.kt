package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.ui.home.HomeState
import com.lyubishchev.timekeeper.ui.home.HomeViewModel
import com.lyubishchev.timekeeper.ui.home.RadarDimension
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.home.RadarPeriod
import com.lyubishchev.timekeeper.ui.theme.NumberSerif
import com.lyubishchev.timekeeper.ui.widget.ChoiceRow
import com.lyubishchev.timekeeper.ui.widget.RadarChart
import com.lyubishchev.timekeeper.ui.widget.radarDashIntervals
import com.lyubishchev.timekeeper.ui.widget.radarPreviousColor
import java.time.LocalDate

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
            text = longDate(state.dateText),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        // 今日 / 本周 / 本月 三条合计并排一行，与雷达图的时间档一一对应
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            StatBlock(
                label = stringResource(R.string.home_stat_today),
                value = TimeRules.formatMinutesCompact(state.todayMinutes),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = stringResource(R.string.home_stat_week),
                value = TimeRules.formatMinutesCompact(state.weekMinutes),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = stringResource(R.string.home_stat_month),
                value = TimeRules.formatMinutesCompact(state.monthMinutes),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
        // 固定两类属于系统文字：标签随界面语言走，数据库里仍按原名匹配
        ChoiceRow(
            options = RadarDimension.entries.toList(),
            selected = state.dimension,
            labelOf = { TimeRules.categoryLabel(CategoryStore.nameAt(if (it == RadarDimension.CLASS_II) 1 else 0)) },
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

/**
 * 图例与雷达图同一套编码：当期=主色实线段+实心圆点，对比期=radarPreviousColor 的虚线段、无圆点。
 * 虚线疏密走 RadarChart 的 radarDashIntervals，两处共用一份参数。
 */
@Composable
private fun RadarLegend(currentLabel: String, previousLabel: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendSolidLine(color = scheme.primary)
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 6.dp),
        )
        Spacer(modifier = Modifier.width(18.dp))
        LegendDashedLine(color = radarPreviousColor(scheme))
        Text(
            text = previousLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/** 当期图例：2dp 实线段 + 3dp 实心圆点，与图内 drawSeries 一致 */
@Composable
private fun LegendSolidLine(color: Color) {
    Canvas(modifier = Modifier.size(width = 20.dp, height = 7.dp)) {
        val y = size.height / 2f
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(size.width / 2f, y))
    }
}

/** 对比期图例：1.5dp 虚线段、无圆点，dash 参数与雷达图共用 */
@Composable
private fun LegendDashedLine(color: Color) {
    Canvas(modifier = Modifier.size(width = 20.dp, height = 7.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(radarDashIntervals()),
        )
    }
}

@StringRes
private fun periodLabel(period: RadarPeriod): Int = when (period) {
    RadarPeriod.TODAY -> R.string.home_period_today
    RadarPeriod.WEEK -> R.string.home_period_week
    RadarPeriod.MONTH -> R.string.home_period_month
}

private fun longDate(isoDate: String): String =
    LocalDate.parse(isoDate, TimeRules.DATE)
        .format(AppLocale.dateFormatter(R.string.pattern_date_long))
