package com.lyubishchev.timekeeper.ui.screen

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.ui.export.ExportScreen
import com.lyubishchev.timekeeper.ui.mine.MineViewModel
import com.lyubishchev.timekeeper.ui.theme.ThemeMode
import com.lyubishchev.timekeeper.ui.theme.ThemePrefs

/**
 * 我的：可点列表形式的入口。当前两项——主题与设置（占位）。
 * Mine: a plain tappable list. Theme picker (system/light/dark) and a settings placeholder.
 */
@Composable
fun MineScreen(
    viewModel: MineViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showCategoryEditor by rememberSaveable { mutableStateOf(false) }
    var showQuickEditor by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var showStyleDialog by rememberSaveable { mutableStateOf(false) }

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
            Text(
                text = stringResource(R.string.mine_stat_entries, viewModel.entryCount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 10.dp),
            )
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
                value = stringResource(R.string.mine_category_value),
                onClick = { showCategoryEditor = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_quick),
                value = stringResource(R.string.mine_quick_value),
                onClick = { showQuickEditor = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_export),
                value = stringResource(R.string.mine_export_value),
                onClick = { showExport = true },
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
                value = stringResource(R.string.mine_style_value),
                onClick = { showStyleDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow(
                title = stringResource(R.string.mine_settings),
                value = stringResource(R.string.mine_settings_value),
                onClick = { showSettingsDialog = true },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.mine_settings)) },
            text = { Text(stringResource(R.string.mine_settings_soon)) },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (showStyleDialog) {
        AlertDialog(
            onDismissRequest = { showStyleDialog = false },
            title = { Text(stringResource(R.string.mine_style)) },
            text = {
                Column {
                    Text(stringResource(R.string.mine_style_soon))
                    listOf("小清新", "都市", "沉稳商务").forEach { name ->
                        Text(
                            text = "· $name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStyleDialog = false }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
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
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(6.dp))
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
