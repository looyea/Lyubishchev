package com.lyubishchev.timekeeper.ui.add

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.domain.TimeRules
import kotlinx.coroutines.launch

/**
 * 「记一笔」表单。字段与 Web 版 TimeEntry 一一对应。
 * The add-record form, mirroring the web client's TimeEntry fields.
 */
data class AddRecordForm(
    val date: String = TimeRules.todayText(),
    /** 默认记当天；只有打开补录才允许改日期 */
    val backfillEnabled: Boolean = false,
    val startTime: String = "",
    val endTime: String = "",
    val category: String = TimeRules.CATEGORY_L1,
    val event: String = "",
    val note: String = "",
    val saving: Boolean = false,
    val feedback: String? = null,
) {
    val events: List<String> get() = TimeRules.eventsFor(category)

    val durationMinutes: Int
        get() = if (startTime.isBlank() || endTime.isBlank()) 0
        else TimeRules.durationMinutes(startTime, endTime)
}

class AddRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TimekeeperApp).repository

    var form by mutableStateOf(AddRecordForm())
        private set

    init {
        // 开始时间默认承接当天上一条的结束时间
        // Seed the start time from the previous entry of the same day.
        viewModelScope.launch {
            repository.lastEndTime(form.date)?.let { previousEnd ->
                form = form.copy(startTime = previousEnd)
            }
        }
    }

    fun onDateChange(date: String) {
        form = form.copy(date = date, endTime = "")
        viewModelScope.launch {
            repository.lastEndTime(date)?.let { previousEnd ->
                form = form.copy(startTime = previousEnd)
            }
        }
    }

    /** 关掉补录就锁回当天，并重新承接当天上一条的结束时间 */
    fun onBackfillChange(enabled: Boolean) {
        form = form.copy(backfillEnabled = enabled)
        if (!enabled && form.date != TimeRules.todayText()) onDateChange(TimeRules.todayText())
    }

    /** 每次进入记一笔调用：VM 跨午夜存活时把"当天"追认为真实今天 */
    fun alignWithToday() {
        val t = TimeRules.todayText()
        if (!form.backfillEnabled && form.date != t) onDateChange(t)
    }

    fun onStartChange(time: String) {
        form = form.copy(
            startTime = time,
            // 结束早于开始视为无效，直接清空
            // Drop an end time that is no longer after the start.
            endTime = if (time.isNotBlank() && form.endTime.isNotBlank() && form.endTime <= time) "" else form.endTime,
        )
    }

    fun onEndChange(time: String) {
        form = form.copy(
            endTime = if (time.isNotBlank() && time <= form.startTime) "" else time,
        )
    }

    fun onCategoryChange(category: String) {
        form = form.copy(category = category, event = "")
    }

    fun onEventChange(event: String) {
        form = form.copy(event = event)
    }

    fun onNoteChange(note: String) {
        form = form.copy(note = note)
    }

    /**
     * 快捷组合：按分钟数从当前开始时间推出结束时间。
     * Quick pick: derive the end time from the start plus a fixed duration.
     */
    fun onQuickPick(category: String, event: String, minutes: Int) {
        val start = form.startTime.ifBlank { TimeRules.now() }
        val end = TimeRules.plusMinutes(start, minutes)
        form = form.copy(category = category, event = event, startTime = start, endTime = end)
    }

    fun save() {
        val current = form
        when {
            current.startTime.isBlank() || current.endTime.isBlank() -> {
                form = current.copy(feedback = FEEDBACK_TIME_REQUIRED)
                return
            }

            current.event.isBlank() -> {
                form = current.copy(feedback = FEEDBACK_EVENT_REQUIRED)
                return
            }

            current.durationMinutes <= 0 -> {
                form = current.copy(feedback = FEEDBACK_ORDER)
                return
            }
        }

        form = current.copy(saving = true, feedback = null)
        viewModelScope.launch {
            val minutes = repository.add(
                date = current.date,
                startTime = current.startTime,
                endTime = current.endTime,
                category = current.category,
                event = current.event,
                note = current.note,
            )
            // 保存后继续登记：结束时间变成下一条的开始时间
            // Keep logging: the end time becomes the next start time.
            form = form.copy(
                startTime = current.endTime,
                endTime = "",
                note = "",
                saving = false,
                feedback = "已记录 ${TimeRules.formatMinutes(minutes)}",
            )
        }
    }

    fun consumeFeedback() {
        form = form.copy(feedback = null)
    }

    companion object {
        const val FEEDBACK_TIME_REQUIRED = "time"
        const val FEEDBACK_EVENT_REQUIRED = "event"
        const val FEEDBACK_ORDER = "order"
    }
}
