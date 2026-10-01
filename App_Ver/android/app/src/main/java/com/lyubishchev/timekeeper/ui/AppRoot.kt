package com.lyubishchev.timekeeper.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.ui.screen.AddRecordScreen
import com.lyubishchev.timekeeper.ui.screen.ComingSoonScreen
import com.lyubishchev.timekeeper.ui.screen.HomeScreen
import com.lyubishchev.timekeeper.ui.screen.LogScreen
import com.lyubishchev.timekeeper.ui.screen.MineScreen

/**
 * 应用外壳：底部五入口导航 + 当前页切换。
 * App shell: five-slot bottom bar plus the current destination.
 *
 * 说明：暂时用索引切页，等 Phase 3 页面变多再换成 Navigation Compose。
 * Tab index switching for now; Navigation Compose arrives in Phase 3 when routes multiply.
 */
@Composable
fun AppRoot() {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // 各页面自行处理状态栏/导航栏内边距，避免双重留白
        // Each screen handles its own bar insets, so the scaffold adds none.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomBar(
                selected = currentTab,
                onSelect = { currentTab = it },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                AppTab.Home -> HomeScreen()
                AppTab.Log -> LogScreen()
                AppTab.Add -> AddRecordScreen()
                AppTab.Report -> ComingSoonScreen(R.string.tab_report)
                AppTab.Mine -> MineScreen()
            }
        }
    }
}
