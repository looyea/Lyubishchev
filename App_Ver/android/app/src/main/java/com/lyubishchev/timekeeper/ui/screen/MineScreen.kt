package com.lyubishchev.timekeeper.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.AppMeta
import com.lyubishchev.timekeeper.domain.BuiltinTemplate
import com.lyubishchev.timekeeper.domain.ReportTemplateStore
import com.lyubishchev.timekeeper.i18n.AppLanguage
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.export.ExportScreen
import com.lyubishchev.timekeeper.ui.export.ExportViewModel
import com.lyubishchev.timekeeper.ui.export.ImportViewModel
import com.lyubishchev.timekeeper.ui.mine.MineViewModel
import com.lyubishchev.timekeeper.ui.theme.ThemeMode
import com.lyubishchev.timekeeper.ui.theme.ThemePalette
import com.lyubishchev.timekeeper.ui.theme.ThemePrefs

/**
 * 设置：可点列表形式的入口——时间分类、快捷设置、按需/全量导出、全量导入、颜色模式、主题配色、版本。
 * Settings: a plain tappable list; full export writes the whole DB to one .db/.csv,
 * and the version row opens the release-note history.
 */
@Composable
fun MineScreen(
    viewModel: MineViewModel = viewModel(),
    exportViewModel: ExportViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val importVm: ImportViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.refresh() }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showCategoryEditor by rememberSaveable { mutableStateOf(false) }
    var showQuickEditor by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var showTemplateScreen by rememberSaveable { mutableStateOf(false) }
    var showAiDialog by rememberSaveable { mutableStateOf(false) }
    var showStyleDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showVersionHistory by rememberSaveable { mutableStateOf(false) }
    var showFullExportDialog by rememberSaveable { mutableStateOf(false) }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { importVm.import(it) } }

    if (showCategoryEditor) {
        CategoryEditorScreen(onBack = { showCategoryEditor = false })
        return
    }
    if (showQuickEditor) {
        QuickPickEditorScreen(onBack = { showQuickEditor = false })
        return
    }
    if (showExport) {
        ExportScreen(onBack = { showExport = false })
        return
    }
    if (showTemplateScreen) {
        ReportTemplateScreen(onBack = { showTemplateScreen = false })
        return
    }
    if (showVersionHistory) {
        VersionHistoryScreen(onBack = { showVersionHistory = false })
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.mine_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(18.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.mine_header_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                val days = AppMeta.daysTogether(context)
                HeaderStat(pluralStringResource(R.plurals.mine_stat_days, days, days))
                HeaderStat(
                    stringResource(
                        R.string.mine_stat_total,
                        viewModel.totalMinutes / 60,
                        viewModel.totalMinutes % 60,
                    ),
                )
                HeaderStat(
                    pluralStringResource(
                        R.plurals.mine_stat_entries,
                        viewModel.entryCount,
                        viewModel.entryCount,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
        ) {
            SettingRow(
                title = stringResource(R.string.mine_category),
                onClick = { showCategoryEditor = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_quick),
                onClick = { showQuickEditor = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_export),
                onClick = { showExport = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_template),
                value = templateLabel(),
                onClick = { showTemplateScreen = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_ai_api),
                value = stringResource(R.string.mine_ai_api_value),
                onClick = { showAiDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_export_all),
                value = stringResource(
                    if (exportViewModel.busy) R.string.export_busy else R.string.mine_export_all_value,
                ),
                onClick = { showFullExportDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_import),
                value = stringResource(
                    if (importVm.busy) R.string.import_busy else R.string.mine_import_value,
                ),
                onClick = { pickFile.launch(arrayOf("*/*")) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_theme),
                value = stringResource(themeModeLabel(ThemePrefs.mode)),
                onClick = { showThemeDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_style),
                value = stringResource(paletteLabel(ThemePrefs.palette)),
                onClick = { showStyleDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_language),
                value = stringResource(
                    if (AppLocale.choice == AppLanguage.SYSTEM) R.string.language_system
                    else languageLabel(AppLocale.choice),
                ),
                onClick = { showLanguageDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_version),
                value = AppVersions.CURRENT,
                onClick = { showVersionHistory = true },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    LaunchedEffect(exportViewModel.shareTrigger) {
        if (exportViewModel.shareTrigger > 0) {
            exportViewModel.shareIntent()?.let { intent ->
                context.startActivity(
                    android.content.Intent.createChooser(
                        intent,
                        context.getString(R.string.export_all_share_title),
                    ),
                )
            }
            exportViewModel.consumeShare()
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            onPick = { chosen ->
                ThemePrefs.set(context, chosen)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showStyleDialog) {
        PalettePickerDialog(
            onPick = { chosen ->
                ThemePrefs.set(context, chosen)
                showStyleDialog = false
            },
            onDismiss = { showStyleDialog = false },
        )
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            onPick = { chosen ->
                AppLocale.setLanguage(context, chosen)
                showLanguageDialog = false
                // 语言是靠 Activity 的 override configuration 生效的，重建一次整棵树才会换文案
                (context as? android.app.Activity)?.recreate()
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = { Text(stringResource(R.string.mine_ai_api)) },
            text = { Text(stringResource(R.string.mine_ai_api_dialog)) },
            confirmButton = {
                TextButton(onClick = { showAiDialog = false }) { Text(stringResource(R.string.dialog_ok)) }
            },
        )
    }

    if (showFullExportDialog) {
        AlertDialog(
            onDismissRequest = { showFullExportDialog = false },
            title = { Text(stringResource(R.string.mine_export_all)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.export_all_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FullExportOption(stringResource(R.string.export_all_db)) {
                        showFullExportDialog = false
                        exportViewModel.exportDatabase()
                    }
                    FullExportOption(stringResource(R.string.export_all_csv)) {
                        showFullExportDialog = false
                        exportViewModel.exportAll()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFullExportDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    importVm.outcome?.let { o ->
        AlertDialog(
            onDismissRequest = { importVm.dismissOutcome() },
            title = { Text(stringResource(R.string.mine_import)) },
            text = { Text(stringResource(R.string.import_result_text, o.added, o.skipped, o.parsed)) },
            confirmButton = {
                TextButton(onClick = { importVm.dismissOutcome() }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    importVm.error?.let { message ->
        AlertDialog(
            onDismissRequest = { importVm.dismissError() },
            title = { Text(stringResource(R.string.mine_import)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { importVm.dismissError() }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }
}

@Composable
private fun FullExportOption(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

/** 设置里的一行：标题左对齐，说明文字右对齐，末尾那枚进入次级页的箭头始终贴着右边缘。 */
@Composable
private fun SettingRow(title: String, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            // fill 必须为 true：不填满权重槽位的话，箭头会紧跟在文字后面，看着像往左偏了一截
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.4f),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemePickerDialog(onPick: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mine_theme_dialog_title)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPick(mode) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = mode == ThemePrefs.mode,
                            onClick = { onPick(mode) },
                        )
                        Text(
                            text = stringResource(themeModeLabel(mode)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.mine_theme_system
    ThemeMode.LIGHT -> R.string.mine_theme_light
    ThemeMode.DARK -> R.string.mine_theme_dark
}

/** 主题选择：列出全部控件配色，每套给出主/辅/第三色的色卡。 */
@Composable
private fun PalettePickerDialog(onPick: (ThemePalette) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mine_style_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ThemePalette.entries.forEach { palette ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPick(palette) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = palette == ThemePrefs.palette,
                            onClick = { onPick(palette) },
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(
                                text = stringResource(paletteLabel(palette)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Row(modifier = Modifier.padding(top = 6.dp)) {
                                PaletteDot(palette.day.primary)
                                PaletteDot(palette.day.secondary)
                                PaletteDot(palette.day.tertiary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun PaletteDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .size(16.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/** 顶部统计卡里的一行：陪伴天数 / 累计时长 / 记录条数。 */
@Composable
private fun HeaderStat(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

/** 设置行右侧显示当前选用的报告模板名。 */
@Composable
private fun templateLabel(): String {
    val selected = ReportTemplateStore.selectedId
    val builtin = BuiltinTemplate.entries.firstOrNull { it.id == selected }
    if (builtin != null) return stringResource(builtin.labelRes)
    return ReportTemplateStore.custom.firstOrNull { it.uri == selected }?.name
        ?: stringResource(R.string.tpl_builtin_md)
}

@StringRes
private fun paletteLabel(palette: ThemePalette): Int = when (palette) {
    ThemePalette.MORANDI -> R.string.mine_palette_morandi
    ThemePalette.TIFFANY -> R.string.mine_palette_tiffany
    ThemePalette.OCEAN -> R.string.mine_palette_ocean
    ThemePalette.FOREST -> R.string.mine_palette_forest
    ThemePalette.CRIMSON -> R.string.mine_palette_crimson
    ThemePalette.AMBER -> R.string.mine_palette_amber
    ThemePalette.ROSE -> R.string.mine_palette_rose
    ThemePalette.GRAPHITE -> R.string.mine_palette_graphite
    ThemePalette.OBSIDIAN -> R.string.mine_palette_obsidian
    ThemePalette.SOLARIZED -> R.string.mine_palette_solarized
}

/** 语言选择：跟随系统 + 七种内置语言，每种都用本族语写出自己的名字。 */
@Composable
private fun LanguagePickerDialog(onPick: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.language_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                AppLanguage.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPick(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == AppLocale.choice,
                            onClick = { onPick(option) },
                        )
                        Text(
                            text = stringResource(
                                if (option == AppLanguage.SYSTEM) R.string.language_system
                                else languageLabel(option),
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@StringRes
private fun languageLabel(language: AppLanguage): Int = when (language) {
    AppLanguage.SYSTEM -> R.string.language_system
    AppLanguage.ZH -> R.string.language_zh
    AppLanguage.EN -> R.string.language_en
    AppLanguage.ES -> R.string.language_es
    AppLanguage.PT -> R.string.language_pt
    AppLanguage.DE -> R.string.language_de
    AppLanguage.NL -> R.string.language_nl
    AppLanguage.JA -> R.string.language_ja
}
