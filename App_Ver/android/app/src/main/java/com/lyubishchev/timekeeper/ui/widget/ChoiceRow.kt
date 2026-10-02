package com.lyubishchev.timekeeper.ui.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 一行互斥分段按钮，概览的切换与记录页的 日/周/月/年 共用。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                // 选中态只靠底色区分，去掉 M3 默认那颗前导对勾
                icon = {},
            ) {
                Text(labelOf(option), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
