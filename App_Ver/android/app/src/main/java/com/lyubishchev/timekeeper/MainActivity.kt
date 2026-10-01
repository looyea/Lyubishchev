package com.lyubishchev.timekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lyubishchev.timekeeper.ui.AppRoot
import com.lyubishchev.timekeeper.ui.theme.TimekeeperTheme

/**
 * 唯一 Activity，全部内容交给 Compose。
 * The single activity; Compose owns the whole UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TimekeeperTheme {
                AppRoot()
            }
        }
    }
}
