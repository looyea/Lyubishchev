package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.CategoryStore
import com.lyubishchev.timekeeper.domain.TimeRules

/**
 * 时间分类维护：分类固定两类（写死），每类下最多 8 个事件；事件可新建、改名、删除。
 * 统计按名字匹配：改名等于新建（从零累计），保留原名的继续累加历史。
 */
@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember { mutableStateOf<InputConfig?>(null) }
    var pendingDelete by remember { mutableStateOf<DeleteConfig?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    val maxChildrenHint = stringResource(R.string.cat_max_children)

    LaunchedEffect(notice) {
        if (notice != null) {
            kotlinx.coroutines.delay(2_500)
            notice = null
        }
    }

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
                text = stringResource(R.string.cat_editor_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = stringResource(R.string.cat_editor_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        CategoryStore.categories.forEachIndexed { ci, cat ->
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = TimeRules.roman(cat.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = TimeRules.categoryLabel(cat.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }

                if (cat.events.isEmpty()) {
                    Text(
                        text = stringResource(R.string.log_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                cat.events.forEachIndexed { ei, event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MiniAction(R.string.cat_rename) {
                            input = InputConfig(
                                titleRes = R.string.cat_rename,
                                initial = event.name,
                                onConfirm = { CategoryStore.renameEvent(ci, ei, it) },
                            )
                        }
                        MiniAction(R.string.cat_delete) {
                            pendingDelete = DeleteConfig(
                                name = event.name,
                                onConfirm = { CategoryStore.deleteEvent(ci, ei) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (cat.events.size >= CategoryStore.MAX_CHILDREN) {
                            notice = maxChildrenHint
                        } else {
                            input = InputConfig(
                                titleRes = R.string.cat_add_event,
                                initial = "",
                                onConfirm = { CategoryStore.addEvent(ci, it) },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.cat_add_event))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    input?.let { config ->
        var text by remember(config) { mutableStateOf(config.initial) }
        AlertDialog(
            onDismissRequest = { input = null },
            title = { Text(stringResource(config.titleRes)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.cat_input_title)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = text.trim()
                        if (name.isNotEmpty()) {
                            config.onConfirm(name)
                            input = null
                        }
                    },
                ) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { input = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    pendingDelete?.let { del ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.cat_delete_confirm_title)) },
            text = { Text(stringResource(R.string.cat_delete_confirm_text, del.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        del.onConfirm()
                        pendingDelete = null
                    },
                ) { Text(stringResource(R.string.cat_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    notice?.let { message ->
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MiniAction(@StringRes labelRes: Int, onClick: () -> Unit) {
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
    )
}

private class InputConfig(
    @StringRes val titleRes: Int,
    val initial: String,
    val onConfirm: (String) -> Unit,
)

private class DeleteConfig(
    val name: String,
    val onConfirm: () -> Unit,
)
