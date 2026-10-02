package com.lyubishchev.timekeeper.ui.screen

import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules

/**
 * 常用配置：每条 = 一级分类 + 二级事件 + 持续时间，供「记一笔」一键套用。
 * Quick-pick editor: category + event + fixed duration, consumed by AddRecordScreen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickPickEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdd by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.pick_editor_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = stringResource(R.string.pick_editor_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.pick_add_title))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CategoryStore.quickPicks.forEachIndexed { pi, pick ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.pick_item_label,
                        TimeRules.categoryLabel(pick.category),
                        pick.event,
                        TimeRules.formatMinutes(pick.minutes),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.cat_delete),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { CategoryStore.deletePick(pi) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAdd) {
        AddPickDialog(onDismiss = { showAdd = false })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPickDialog(onDismiss: () -> Unit) {
    var category by remember { mutableStateOf(CategoryStore.nameAt(0)) }
    var event by remember { mutableStateOf<String?>(null) }
    var minutes by remember { mutableStateOf(30) }

    val events = CategoryStore.eventsFor(category)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_add_title)) },
        text = {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = stringResource(R.string.pick_category_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryStore.categoryNames().forEach { name ->
                        FilterChip(
                            selected = name == category,
                            onClick = {
                                category = name
                                event = null
                            },
                            label = { Text(TimeRules.categoryLabel(name)) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.pick_event_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (events.isEmpty()) {
                    Text(
                        text = stringResource(R.string.pick_no_event),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        events.forEach { name ->
                            FilterChip(
                                selected = name == event,
                                onClick = { event = name },
                                label = { Text(name) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.pick_minutes_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QUICK_MINUTES.forEach { m ->
                        FilterChip(
                            selected = m == minutes,
                            onClick = { minutes = m },
                            label = { Text(TimeRules.formatMinutes(m)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = event != null,
                onClick = {
                    event?.let {
                        CategoryStore.addPick(category, it, minutes)
                        onDismiss()
                    }
                },
            ) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

private val QUICK_MINUTES = listOf(15, 30, 45, 60, 90, 120, 180)
