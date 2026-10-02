package com.lyubishchev.timekeeper.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.BuiltinTemplate
import com.lyubishchev.timekeeper.domain.CustomTemplate
import com.lyubishchev.timekeeper.domain.ReportTemplateStore

/**
 * 报告模板：选择内置模板或上传自己的模板文件。
 * 本版只登记与选用，模板内容的解析渲染留给后续版本；格式限定 .md / .txt。
 */
@Composable
fun ReportTemplateScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var notice by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CustomTemplate?>(null) }

    val pickTemplate = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val file = CustomTemplate(displayName(context, uri), uri.toString())
            if (file.name.endsWith(".md", ignoreCase = true) || file.name.endsWith(".txt", ignoreCase = true)) {
                ReportTemplateStore.add(context, file)
                ReportTemplateStore.select(context, file.uri)
                notice = context.getString(R.string.tpl_uploaded, file.name)
            } else {
                notice = context.getString(R.string.tpl_err_format)
            }
        }
    }

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
                text = "‹ " + stringResource(R.string.tpl_back),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(top = 6.dp, end = 12.dp, bottom = 6.dp),
            )
            Text(
                text = stringResource(R.string.tpl_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = stringResource(R.string.tpl_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(stringResource(R.string.tpl_section_builtin))
        BuiltinTemplate.entries.forEach { template ->
            TemplateRow(
                label = stringResource(template.labelRes),
                desc = stringResource(template.descRes),
                badge = if (template == BuiltinTemplate.MARKDOWN) stringResource(R.string.tpl_default_badge) else null,
                selected = ReportTemplateStore.selectedId == template.id,
                onSelect = { ReportTemplateStore.select(context, template.id) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(stringResource(R.string.tpl_section_custom))
        if (ReportTemplateStore.custom.isEmpty()) {
            Text(
                text = stringResource(R.string.tpl_custom_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            ReportTemplateStore.custom.forEach { template ->
                TemplateRow(
                    label = template.name,
                    desc = template.uri,
                    badge = null,
                    selected = ReportTemplateStore.selectedId == template.uri,
                    onSelect = { ReportTemplateStore.select(context, template.uri) },
                    onDelete = { pendingDelete = template },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = { pickTemplate.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.tpl_upload))
        }
        Text(
            text = stringResource(R.string.tpl_upload_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        notice?.let { message ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    pendingDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.tpl_delete_title)) },
            text = { Text(stringResource(R.string.tpl_delete_text, template.name)) },
            confirmButton = {
                TextButton(onClick = {
                    ReportTemplateStore.remove(context, template.uri)
                    pendingDelete = null
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun TemplateRow(
    label: String,
    desc: String,
    badge: String?,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface,
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                badge?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (onDelete != null) {
            TextButton(onClick = onDelete) {
                Text(
                    text = stringResource(R.string.tpl_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** 从 SAF URI 拿显示名；系统给不出就退回路径末段 */
private fun displayName(context: android.content.Context, uri: Uri): String {
    var name: String? = null
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) name = cursor.getString(index)
        }
    }
    persistReadPermission(context, uri)
    return name ?: uri.lastPathSegment?.substringAfterLast('/') ?: uri.toString()
}

/** 留下可读授权，后续版本才能真的读这份模板 */
private fun persistReadPermission(context: android.content.Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
}
