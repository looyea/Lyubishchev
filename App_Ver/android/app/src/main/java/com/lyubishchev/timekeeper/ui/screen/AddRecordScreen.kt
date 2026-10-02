package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.add.AddRecordViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 记一笔：与 Web 版 TimeEntry 同字段的移动表单（日期 / 起止 / 时长 / 分类 / 事件 / 备注 / 常用）。
 * Add record: same fields as the web TimeEntry form, laid out for one-handed use.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecordScreen(
    viewModel: AddRecordViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val form = viewModel.form
    LaunchedEffect(Unit) { viewModel.alignWithToday() }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.add_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.add_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 一行：日期 + 「补充记录」Label + CheckBox；只有勾选补录才允许改日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = longDate(form.date),
                style = MaterialTheme.typography.titleMedium,
                color = if (form.backfillEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .clickable(enabled = form.backfillEnabled) { showDatePicker = true }
                    .padding(vertical = 6.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.add_backfill_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(
                checked = form.backfillEnabled,
                onCheckedChange = viewModel::onBackfillChange,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 开始/结束各占一行：label 在左，点右侧时间弹出各自的 TimePicker
        TimeField(R.string.field_start, form.startTime) { pickStart = true }
        Spacer(modifier = Modifier.height(4.dp))
        TimeField(R.string.field_end, form.endTime) { pickEnd = true }

        Spacer(modifier = Modifier.height(12.dp))

        // 时长（自动计算）/ derived duration：固定 label + 数值
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.add_duration_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (form.durationMinutes > 0) {
                    TimeRules.formatMinutes(form.durationMinutes)
                } else {
                    stringResource(R.string.add_duration_pending)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (form.durationMinutes > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        FieldLabel(R.string.field_category)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeRules.CATEGORIES.forEach { category ->
                FilterChip(
                    selected = form.category == category,
                    onClick = { viewModel.onCategoryChange(category) },
                    label = { Text(TimeRules.categoryLabel(category)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FieldLabel(R.string.field_event)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            form.events.forEach { event ->
                FilterChip(
                    selected = form.event == event,
                    onClick = { viewModel.onEventChange(event) },
                    label = { Text(event) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = form.note,
            onValueChange = viewModel::onNoteChange,
            label = { Text(stringResource(R.string.field_note)) },
            placeholder = { Text(stringResource(R.string.add_note_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        FieldLabel(R.string.add_quick_label)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryStore.quickPicks.forEach { pick ->
                AssistChip(
                    onClick = { viewModel.onQuickPick(pick.category, pick.event, pick.minutes) },
                    label = { Text("${pick.event} ${TimeRules.formatMinutes(pick.minutes)}") },
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = viewModel::save,
            enabled = !form.saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.add_save), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(form.date, TimeRules.DATE)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateChange(
                                LocalDate.ofEpochDay(millis / 86_400_000L).format(TimeRules.DATE)
                            )
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (pickStart) {
        TimePickerDialogField(
            titleRes = R.string.field_start,
            initial = form.startTime,
            onDismiss = { pickStart = false },
            onConfirm = {
                viewModel.onStartChange(it)
                pickStart = false
            },
        )
    }
    if (pickEnd) {
        TimePickerDialogField(
            titleRes = R.string.field_end,
            initial = form.endTime,
            onDismiss = { pickEnd = false },
            onConfirm = {
                viewModel.onEndChange(it)
                pickEnd = false
            },
        )
    }

    // 提示条：保存成功或校验失败，两秒后自动消失
    // Transient feedback banner, auto cleared.
    form.feedback?.let { code ->
        val message = when (code) {
            AddRecordViewModel.FEEDBACK_TIME_REQUIRED -> stringResource(R.string.add_err_time)
            AddRecordViewModel.FEEDBACK_EVENT_REQUIRED -> stringResource(R.string.add_err_event)
            AddRecordViewModel.FEEDBACK_ORDER -> stringResource(R.string.add_err_order)
            else -> code
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
        LaunchedEffect(code) {
            delay(2_500)
            viewModel.consumeFeedback()
        }
    }
}

@Composable
private fun FieldLabel(@StringRes res: Int) {
    Text(
        text = stringResource(res),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

/** 时间行：左 label，右侧当前值，点击弹出该字段自己的 TimePicker */
@Composable
private fun TimeField(@StringRes labelRes: Int, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value.ifBlank { stringResource(R.string.add_time_pending) },
            style = MaterialTheme.typography.headlineSmall,
            color = if (value.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogField(
    @StringRes titleRes: Int,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val timeState = rememberTimePickerState(
        initialHour = initial.takeIf { it.length == 5 }?.substring(0, 2)?.toIntOrNull()
            ?: currentHour(),
        initialMinute = initial.takeIf { it.length == 5 }?.substring(3, 5)?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                TimePicker(state = timeState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                    TextButton(
                        onClick = {
                            onConfirm("%02d:%02d".format(timeState.hour, timeState.minute))
                        },
                    ) { Text(stringResource(R.string.dialog_ok)) }
                }
            }
        }
    }
}

private fun longDate(isoDate: String): String =
    LocalDate.parse(isoDate, TimeRules.DATE)
        .format(AppLocale.dateFormatter(R.string.pattern_date_long))

private fun currentHour(): Int = java.time.LocalTime.now().hour
