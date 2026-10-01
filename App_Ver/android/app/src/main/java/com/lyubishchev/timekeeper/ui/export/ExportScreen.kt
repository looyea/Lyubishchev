package com.lyubishchev.timekeeper.ui.export

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.ExportFormat
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.ui.widget.ChoiceRow
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 导出：选起止日期 + 格式（MD/TXT/Excel/HTML），先给区间汇总预览，
 * 生成后写入应用目录并拉起系统分享面板。
 * Export screen: pick a closed date range + format, preview the summary, then write
 * a file into the app external dir and open the share sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    var pickingFrom by remember { mutableStateOf(false) }
    var pickingTo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "‹ " + stringResource(R.string.cat_back),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(top = 6.dp, end = 12.dp, bottom = 6.dp),
            )
            Text(
                text = stringResource(R.string.export_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = stringResource(R.string.export_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.export_range_label),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { pickingFrom = true }) {
                Text(viewModel.fromDate)
            }
            Text(
                text = " ～ ",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { pickingTo = true }) {
                Text(viewModel.toDate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.export_format_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ChoiceRow(
            options = ExportFormat.entries.toList(),
            selected = viewModel.format,
            labelOf = { it.title },
            onSelect = viewModel::onFormatChange,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            Text(
                text = stringResource(R.string.export_summary_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.export_total, TimeRules.formatMinutes(preview.totalMinutes)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            preview.perCategory.forEach { (name, minutes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = TimeRules.formatMinutes(minutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                preview.perEvent[name]?.forEach { (event, minutes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = event,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = TimeRules.formatMinutes(minutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.export_radar_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = viewModel::export,
            enabled = !viewModel.busy && viewModel.fromDate <= viewModel.toDate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = stringResource(
                    if (viewModel.busy) R.string.export_busy else R.string.export_run,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (pickingFrom) {
        ExportDatePicker(viewModel.fromDate) {
            viewModel.onFromChange(it)
            pickingFrom = false
        }
    }
    if (pickingTo) {
        ExportDatePicker(viewModel.toDate) {
            viewModel.onToChange(it)
            pickingTo = false
        }
    }

    when (viewModel.message) {
        "EMPTY" -> ExportNotice(stringResource(R.string.export_empty)) { viewModel.consumeMessage() }
        "INVALID_RANGE" -> ExportNotice(stringResource(R.string.export_invalid_range)) { viewModel.consumeMessage() }
        "DONE" -> ExportNotice(stringResource(R.string.export_saved)) { /* 保留提示直到下次导出 */ }
        else -> {}
    }

    LaunchedEffect(viewModel.shareTrigger) {
        if (viewModel.shareTrigger > 0) {
            viewModel.shareIntent()?.let { intent ->
                context.startActivity(
                    android.content.Intent.createChooser(intent, context.getString(R.string.export_share_title)),
                )
            }
        }
    }
}

@Composable
private fun ExportNotice(text: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4_000)
        onDismiss()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportDatePicker(current: String, onPicked: (String) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.parse(current, TimeRules.DATE)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = { onPicked(current) },
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        onPicked(LocalDate.ofEpochDay(millis / 86_400_000L).format(TimeRules.DATE))
                    } else {
                        onPicked(current)
                    }
                },
            ) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = { onPicked(current) }) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}
