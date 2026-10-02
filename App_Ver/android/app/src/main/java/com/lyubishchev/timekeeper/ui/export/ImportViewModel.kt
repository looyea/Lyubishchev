package com.lyubishchev.timekeeper.ui.export

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.TimekeeperApp
import com.lyubishchev.timekeeper.data.TimeLogEntity
import com.lyubishchev.timekeeper.domain.TimeRules
import com.lyubishchev.timekeeper.i18n.AppLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/** 导入成功后的计数：新增 / 因重复被跳过 / 文件里解析出的总条数 */
data class ImportOutcome(val added: Int, val skipped: Int, val parsed: Int)

/**
 * 全量导入：吃进本 App 全量导出的 .db 或 CSV，逐条与主数据库比对，
 * 同日期 + 同起止 + 同类别事件的记录视为重复自动跳过，其余合并入库。
 */
class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TimekeeperApp).repository

    var busy by mutableStateOf(false)
        private set
    var outcome by mutableStateOf<ImportOutcome?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun dismissOutcome() { outcome = null }
    fun dismissError() { error = null }

    fun import(uri: Uri) {
        if (busy) return
        busy = true
        outcome = null
        error = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val src = copyToCache(uri)
                val rows = if (isSqlite(src)) readFromSqlite(src) else readFromCsv(src)
                if (rows.isEmpty()) {
                    error = AppLocale.str(R.string.import_err_empty)
                    return@launch
                }
                merge(rows)
            } catch (e: Exception) {
                error = e.message ?: AppLocale.str(R.string.import_err_unreadable)
            } finally {
                busy = false
            }
        }
    }

    private fun copyToCache(uri: Uri): File {
        val ctx = getApplication<TimekeeperApp>()
        val dst = File(ctx.cacheDir, "import_src.dat")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            dst.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalArgumentException(AppLocale.str(R.string.import_err_open))
        return dst
    }

    private fun isSqlite(file: File): Boolean {
        val magic = ByteArray(15)
        val read = file.inputStream().use { it.read(magic) }
        return read >= 15 && String(magic, 0, 15, Charsets.ISO_8859_1) == "SQLite format 3"
    }

    // ---------- SQLite ----------

    private data class RawRow(
        val date: String,
        val startTime: String,
        val endTime: String,
        val minutes: Int,
        val category: String,
        val event: String,
        val note: String,
    )

    private fun readFromSqlite(file: File): List<RawRow> {
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='time_logs'", null,
            ).use { c ->
                if (!c.moveToFirst()) throw IllegalArgumentException(AppLocale.str(R.string.import_err_no_table))
            }
            val rows = mutableListOf<RawRow>()
            db.rawQuery(
                "SELECT date, start_time, end_time, duration_minutes, category, event, note FROM time_logs",
                null,
            ).use { c ->
                while (c.moveToNext()) {
                    rows += RawRow(
                        date = c.getString(0) ?: continue,
                        startTime = c.getString(1) ?: "",
                        endTime = c.getString(2) ?: "",
                        minutes = c.getInt(3),
                        category = c.getString(4) ?: "",
                        event = c.getString(5) ?: "",
                        note = c.getString(6) ?: "",
                    )
                }
            }
            return rows
        } finally {
            db.close()
        }
    }

    // ---------- CSV（本 App 按需/全量导出格式） ----------

    private fun readFromCsv(file: File): List<RawRow> {
        val text = file.readText(Charsets.UTF_8).removePrefix("﻿")
        val rows = mutableListOf<RawRow>()
        text.lineSequence().forEach { line ->
            if (line.isBlank()) return@forEach
            val cells = splitCsvLine(line)
            if (cells.size < 7) return@forEach
            parseCsvRow(cells)?.let { rows += it }
        }
        return rows
    }

    /** 年份,月日,星期,类别,事件,开始,结束,持续时长(分钟),持续时长,备注 —— 表头不是数字，自然被丢掉 */
    private fun parseCsvRow(cells: List<String>): RawRow? {
        val year = cells[0].trim().toIntOrNull() ?: return null
        // 月日单元格跟着导出语言变（10月2日 / 10-2 / 10/2），只按数字取
        val parts = cells[1].trim().split(Regex("[^0-9]+")).filter { it.isNotEmpty() }
        if (parts.size < 2) return null
        val month = parts[0].toIntOrNull() ?: return null
        val day = parts[1].toIntOrNull() ?: return null
        val date = "%04d-%02d-%02d".format(year, month, day)
        runCatching { java.time.LocalDate.parse(date, TimeRules.DATE) }.getOrNull() ?: return null
        val start = cells[5].trim()
        val end = cells[6].trim()
        val minutes = cells.getOrNull(7)?.trim()?.toIntOrNull()
            ?: runCatching { TimeRules.durationMinutes(start, end) }.getOrNull() ?: 0
        if (minutes <= 0 || start.isBlank() || end.isBlank()) return null
        val category = cells[3].trim().substringAfter('·').trim()
        return RawRow(
            date = date,
            startTime = start,
            endTime = end,
            minutes = minutes,
            category = category,
            event = cells[4].trim(),
            note = cells.getOrElse(9) { "" }.trim(),
        )
    }

    private fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                quoted -> when {
                    c == '"' && i + 1 < line.length && line[i + 1] == '"' -> { sb.append('"'); i++ }
                    c == '"' -> quoted = false
                    else -> sb.append(c)
                }
                c == '"' -> quoted = true
                c == ',' -> { out += sb.toString(); sb.setLength(0) }
                else -> sb.append(c)
            }
            i++
        }
        out += sb.toString()
        return out
    }

    // ---------- 合并 ----------

    private suspend fun merge(rows: List<RawRow>) {
        val existing = repository.entriesAll().map { it.mergeKey() }.toHashSet()
        val fresh = mutableListOf<TimeLogEntity>()
        for (r in rows) {
            val key = listOf(r.date, r.startTime, r.endTime, r.category, r.event, r.minutes).joinToString("|")
            if (key in existing) continue
            existing += key
            fresh += TimeLogEntity(
                date = r.date,
                startTime = r.startTime,
                endTime = r.endTime,
                durationMinutes = r.minutes,
                category = r.category,
                event = r.event,
                note = r.note,
                weekNumber = TimeRules.isoWeek(r.date),
                year = TimeRules.year(r.date),
            )
        }
        if (fresh.isNotEmpty()) repository.insertAll(fresh)
        outcome = ImportOutcome(added = fresh.size, skipped = rows.size - fresh.size, parsed = rows.size)
    }

    private fun TimeLogEntity.mergeKey(): String =
        listOf(date, startTime, endTime, category, event, durationMinutes).joinToString("|")
}
