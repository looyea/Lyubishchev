package com.lyubishchev.timekeeper.ui.add

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.i18n.AppLocale
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
        val clearsEnd = time.isNotBlank() && form.endTime.isNotBlank() && form.endTime <= time
        form = form.copy(
            startTime = time,
            // 新开始时间不早于已填结束时间时结束时间失效：清空并给出提示，别让人猜
            // Drop a now-invalid end time, but explain why instead of clearing silently.
            endTime = if (clearsEnd) "" else form.endTime,
            feedback = if (clearsEnd) FEEDBACK_ORDER else form.feedback,
        )
    }

    fun onEndChange(time: String) {
        // 结束不晚于开始：不接受，保留原结束时间并用横幅说明原因
        // Reject an end time that is not after the start; keep the old value and explain.
        if (time.isNotBlank() && time <= form.startTime) {
            form = form.copy(feedback = FEEDBACK_ORDER)
            return
        }
        form = form.copy(endTime = time)
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
        // 结束时间最晚 23:59：跨午夜时夹到 23:59，时长按当天剩余分钟计，不再回绕成 00:00
        // Clamp the derived end to 23:59 so a cross-midnight pick never wraps back to 00:00.
        val startMinutes = start.substring(0, 2).toInt() * 60 + start.substring(3, 5).toInt()
        val end = if (startMinutes + minutes >= 24 * 60) "23:59" else TimeRules.plusMinutes(start, minutes)
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

            // 一行防御：夹到 23:59 后理论上不会再出现，但仍挡住"结束早于等于开始"这种回绕后可能变正的情况
            current.durationMinutes <= 0 || current.endTime <= current.startTime -> {
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
                feedback = AppLocale.str(R.string.add_saved, TimeRules.formatMinutes(minutes)),
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
