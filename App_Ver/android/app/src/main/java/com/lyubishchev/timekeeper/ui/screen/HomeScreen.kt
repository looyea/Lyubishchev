package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
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
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.ui.home.HomeState
import com.lyubishchev.timekeeper.ui.home.HomeViewModel
import com.lyubishchev.timekeeper.ui.home.RadarDimension
import com.lyubishchev.timekeeper.ui.home.RadarPeriod
import com.lyubishchev.timekeeper.ui.widget.ChoiceRow
import com.lyubishchev.timekeeper.ui.widget.RadarChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 概览页：日期 + 今日/本周累计，下方一张可切换的雷达图卡片。
 * Home: the date, today's and this week's totals, then one radar card that toggles
 * between class-I/class-II events and between today-vs-yesterday / thisweek-vs-lastweek.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        // 今日 / 本周 两条合计并排一行，样式一致
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

/** 日期下方并排的合计块：上小标签、下主色数值。 */
@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** 雷达图卡片：顶部当期总量，中间图形，底部两组切换。 */
@Composable
private fun RadarCard(
    state: HomeState,
    onDimension: (RadarDimension) -> Unit,
    onPeriod: (RadarPeriod) -> Unit,
) {
    val weekly = state.period == RadarPeriod.WEEK
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = TimeRules.formatMinutes(state.headlineMinutes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))
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
            currentLabel = stringResource(if (weekly) R.string.home_series_week else R.string.home_series_today),
            previousLabel = stringResource(if (weekly) R.string.home_series_last_week else R.string.home_series_yesterday),
            currentMinutes = state.categoryMinutesCurrent,
            previousMinutes = state.categoryMinutesPrevious,
        )

        Spacer(modifier = Modifier.height(18.dp))
        ChoiceRow(
            options = RadarDimension.entries.toList(),
            selected = state.dimension,
            labelOf = { stringResource(dimensionLabel(it)) },
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
private fun RadarLegend(
    currentLabel: String,
    previousLabel: String,
    currentMinutes: Int,
    previousMinutes: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(MaterialTheme.colorScheme.primary)
        Text(
            text = stringResource(R.string.home_radar_legend_item, currentLabel, TimeRules.formatMinutes(currentMinutes)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 6.dp),
        )
        Spacer(modifier = Modifier.width(18.dp))
        LegendDot(MaterialTheme.colorScheme.secondary)
        Text(
            text = stringResource(R.string.home_radar_legend_item, previousLabel, TimeRules.formatMinutes(previousMinutes)),
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
private fun dimensionLabel(dimension: RadarDimension): Int = when (dimension) {
    RadarDimension.CLASS_I -> R.string.home_dim_class_i
    RadarDimension.CLASS_II -> R.string.home_dim_class_ii
}

@StringRes
private fun periodLabel(period: RadarPeriod): Int = when (period) {
    RadarPeriod.TODAY -> R.string.home_period_today
    RadarPeriod.WEEK -> R.string.home_period_week
}

private val CHINESE_DATE: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日 EEEE", Locale.CHINA)

private fun chineseDate(isoDate: String): String =
    LocalDate.parse(isoDate, TimeRules.DATE).format(CHINESE_DATE)
