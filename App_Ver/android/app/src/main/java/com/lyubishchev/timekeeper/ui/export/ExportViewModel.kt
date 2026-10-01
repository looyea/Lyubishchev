package com.lyubishchev.timekeeper.ui.export

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.data.TimeLogRepository
import com.lyubishchev.timekeeper.domain.ExportFormat
import com.lyubishchev.timekeeper.domain.ExportRenderer
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.TimekeeperApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ExportPreview(
    val loading: Boolean = true,
    val entryCount: Int = 0,
    val totalMinutes: Int = 0,
    val perCategory: List<Pair<String, Int>> = emptyList(),
    val perEvent: Map<String, List<Pair<String, Int>>> = emptyMap(),
)

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeLogRepository = (application as TimekeeperApp).repository

    private val today = TimeRules.todayText()

    var fromDate by mutableStateOf(TimeRules.dateMinusDays(today, 29))
        private set
    var toDate by mutableStateOf(today)
        private set
    var format by mutableStateOf(ExportFormat.HTML)
        private set

    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    /** 界面观察这个计数来拉起系统分享面板 */
    var shareTrigger by mutableStateOf(0)
        private set
    private var lastFile: File? = null

    private val range = MutableStateFlow(fromDate to toDate)

    @OptIn(ExperimentalCoroutinesApi::class)
    val preview = range.flatMapLatest { (from, to) ->
        if (from > to) flowOf(ExportPreview(loading = false))
        else repository.breakdownOfRange(from, to).map { rows ->
            val total = rows.sumOf { it.minutes }
            ExportPreview(
                loading = false,
                entryCount = rows.size,
                totalMinutes = total,
                perCategory = com.lyubishchev.timekeeper.domain.CategoryStore.categories.map { c ->
                    c.name to rows.filter { it.category == c.name }.sumOf { it.minutes }
                },
                perEvent = com.lyubishchev.timekeeper.domain.CategoryStore.categories.associate { c ->
                    c.name to c.events.map { e ->
                        e.name to rows.filter { it.category == c.name && it.event == e.name }.sumOf { it.minutes }
                    }.filter { it.second > 0 }
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = ExportPreview(),
    )

    fun onFromChange(date: String) {
        fromDate = date
        range.value = date to toDate
    }

    fun onToChange(date: String) {
        toDate = date
        range.value = fromDate to date
    }

    fun onFormatChange(chosen: ExportFormat) {
        format = chosen
    }

    fun export() {
        if (busy || fromDate > toDate) {
            message = "INVALID_RANGE"
            return
        }
        busy = true
        message = null
        viewModelScope.launch {
            val entries = repository.entriesInRange(fromDate, toDate)
            if (entries.isEmpty()) {
                busy = false
                message = "EMPTY"
                return@launch
            }
            val text = ExportRenderer.render(format, fromDate, toDate, entries)
            val dir = File(getApplication<TimekeeperApp>().getExternalFilesDir(null), "export").apply { mkdirs() }
            val file = File(dir, "time_report_${fromDate}_${toDate}.${format.extension}")
            file.outputStream().use { out ->
                // Excel 兼容：CSV 带 BOM，中文不乱码
                if (format == ExportFormat.EXCEL) out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                out.write(text.toByteArray(Charsets.UTF_8))
            }
            lastFile = file
            busy = false
            message = "DONE"
            shareTrigger++
        }
    }

    fun consumeMessage() {
        message = null
    }

    fun shareIntent(): Intent? {
        val file = lastFile ?: return null
        val uri = FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file,
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = format.mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
