package com.lyubishchev.timekeeper.ui.mine

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.TimekeeperApp
import kotlinx.coroutines.launch

/** 「我的」页只需要一个总条数，冷启动读一次就够 */
class MineViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as TimekeeperApp).repository

    var entryCount by mutableStateOf(0)
        private set

    init {
        viewModelScope.launch { entryCount = repository.count() }
    }
}
