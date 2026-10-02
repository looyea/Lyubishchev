package com.lyubishchev.timekeeper.domain

import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.data.TimeLogEntity
import com.lyubishchev.timekeeper.i18n.AppLocale
import java.time.LocalDate
import java.time.format.TextStyle
import kotlin.math.cos
import kotlin.math.sin

/** 四种导出格式，命名与界面一致 */
enum class ExportFormat(val title: String, val extension: String, val mime: String) {
    MARKDOWN("MD", "md", "text/markdown"),
    TEXT("TXT", "txt", "text/plain"),
    EXCEL("Excel", "csv", "text/csv"),
    HTML("HTML", "html", "text/html"),
}

/** 汇总后的一个类别：名字 + 总分钟 + 各事件分钟 */
data class ExportCategorySummary(
    val name: String,
    val totalMinutes: Int,
    val events: List<Pair<String, Int>>,
)

/**
 * 把区间记录渲染成导出文档：先汇总（总量/一类/二类/各事件/雷达图），
 * 再逐条例：年、月日、星期、类别、事件、起止、时长、备注。
 * Renders the date-range report: summary block first, then one line per entry.
 */
object ExportRenderer {

    fun render(
        format: ExportFormat,
        fromDate: String,
        toDate: String,
        entries: List<TimeLogEntity>,
    ): String {
        val categories = summarize(entries)
        val total = entries.sumOf { it.durationMinutes }
        return when (format) {
            ExportFormat.MARKDOWN -> renderMarkdown(fromDate, toDate, entries, categories, total)
            ExportFormat.TEXT -> renderText(fromDate, toDate, entries, categories, total)
            ExportFormat.EXCEL -> renderCsv(entries)
            ExportFormat.HTML -> renderHtml(fromDate, toDate, entries, categories, total)
        }
    }

    /** 按当前分类树的顺序汇总；树里已删除/改名的名字自然为 0（不予管辖） */
    private fun summarize(entries: List<TimeLogEntity>): List<ExportCategorySummary> =
        CategoryStore.categories.map { cat ->
            val rows = entries.filter { it.category == cat.name }
            ExportCategorySummary(
                name = cat.name,
                totalMinutes = rows.sumOf { it.durationMinutes },
                events = cat.events.map { e ->
                    e.name to rows.filter { it.event == e.name }.sumOf { it.durationMinutes }
                }.filter { it.second > 0 },
            )
        }

    // ---------- 逐条八列 ----------

    private fun entryCells(e: TimeLogEntity): List<String> {
        val day = LocalDate.parse(e.date, TimeRules.DATE)
        return listOf(
            AppLocale.str(R.string.report_year_cell, e.year),
            AppLocale.str(R.string.report_md_cell, day.monthValue, day.dayOfMonth),
            weekday(day),
            AppLocale.str(R.string.report_cat_cell, TimeRules.roman(e.category), e.category),
            e.event,
            "${e.startTime}-${e.endTime}",
            TimeRules.formatMinutes(e.durationMinutes),
            e.note,
        )
    }

    /** 星期跟着语言走（周一 / Mon / Montag…） */
    private fun weekday(day: LocalDate): String =
        day.dayOfWeek.getDisplayName(TextStyle.SHORT, AppLocale.locale)

    // ---------- MD ----------

    private fun renderMarkdown(
        from: String, to: String,
        entries: List<TimeLogEntity>,
        categories: List<ExportCategorySummary>,
        total: Int,
    ): String = buildString {
        appendLine("# ${AppLocale.str(R.string.report_title)}")
        appendLine()
        appendLine(
            "**${AppLocale.str(R.string.report_range)}**：$from ～ $to" +
                "（${AppLocale.str(R.string.report_entries_count, entries.size)}）",
        )
        appendLine()
        appendLine("## ${AppLocale.str(R.string.report_summary)}")
        appendLine()
        appendLine("| ${AppLocale.str(R.string.report_item)} | ${AppLocale.str(R.string.report_col_duration)} |")
        appendLine("| --- | --- |")
        appendLine("| ${AppLocale.str(R.string.report_total)} | ${TimeRules.formatMinutes(total)} |")
        categories.forEach { c ->
            appendLine("| ${c.name} | ${TimeRules.formatMinutes(c.totalMinutes)} |")
        }
        appendLine()
        categories.forEach { c ->
            if (c.events.isNotEmpty()) {
                appendLine("### ${c.name} ${AppLocale.str(R.string.report_col_event)}")
                appendLine()
                appendLine(
                    "| ${AppLocale.str(R.string.report_col_event)} | " +
                        "${AppLocale.str(R.string.report_col_duration)} | ${AppLocale.str(R.string.report_col_share)} |",
                )
                appendLine("| --- | --- | --- |")
                c.events.forEach { (name, m) ->
                    val pct = if (total > 0) m * 100.0 / total else 0.0
                    appendLine("| $name | ${TimeRules.formatMinutes(m)} | %.1f%%".format(pct))
                }
                appendLine()
            }
        }
        appendLine("## ${AppLocale.str(R.string.report_radar)}")
        appendLine()
        appendLine("```")
        categories.forEach { c -> appendRadarAscii(c, categories.maxOfOrNull { s -> s.events.maxOfOrNull { it.second } ?: 0 } ?: 0) }
        appendLine("```")
        appendLine()
        appendLine("## ${AppLocale.str(R.string.report_entries)}")
        appendLine()
        appendLine(entryTableHeader())
        appendLine("| --- | --- | --- | --- | --- | --- | --- | --- |")
        entries.forEach { e ->
            appendLine("| " + entryCells(e).joinToString(" | ") + " |")
        }
    }

    /** 逐条明细的八列表头，跟着语言走 */
    private fun entryTableHeader(): String = listOf(
        R.string.report_col_year,
        R.string.report_col_md,
        R.string.report_col_weekday,
        R.string.report_col_category,
        R.string.report_col_event,
        R.string.report_col_span,
        R.string.report_col_duration,
        R.string.report_col_note,
    ).joinToString(" | ", prefix = "| ", postfix = " |") { AppLocale.str(it) }

    private fun StringBuilder.appendRadarAscii(c: ExportCategorySummary, scaleMax: Int) {
        if (c.events.isEmpty()) return
        val unit = maxOf(1, scaleMax / 10)
        appendLine("${c.name} (${AppLocale.str(R.string.report_scale_note, unit)})")
        c.events.forEach { (name, m) ->
            val bars = m / unit
            appendLine(String.format("%-6s", name).padEnd(8, ' '))
            appendLine("  " + "█".repeat(bars) + " ${TimeRules.formatMinutes(m)}")
        }
        appendLine()
    }

    // ---------- TXT ----------

    private fun renderText(
        from: String, to: String,
        entries: List<TimeLogEntity>,
        categories: List<ExportCategorySummary>,
        total: Int,
    ): String = buildString {
        appendLine(
            "${AppLocale.str(R.string.report_title)}  ${AppLocale.str(R.string.report_range)} " +
                "$from ～ $to  ${AppLocale.str(R.string.report_entries_count, entries.size)}",
        )
        appendLine("=".repeat(48))
        appendLine()
        appendLine("[${AppLocale.str(R.string.report_summary)}]")
        appendLine("${AppLocale.str(R.string.report_total)}: ${TimeRules.formatMinutes(total)}")
        categories.forEach { c ->
            appendLine("${c.name}: ${TimeRules.formatMinutes(c.totalMinutes)}")
        }
        appendLine()
        categories.forEach { c ->
            if (c.events.isNotEmpty()) {
                appendLine("<${c.name} ${AppLocale.str(R.string.report_col_event)}>")
                c.events.forEach { (name, m) ->
                    appendLine("  $name  ${TimeRules.formatMinutes(m)}")
                }
                appendLine()
            }
        }
        appendLine("[${AppLocale.str(R.string.report_entries)}]")
        entries.forEachIndexed { i, e ->
            val cells = entryCells(e)
            appendLine(
                "${i + 1}. ${cells[0]} ${cells[1]} (${cells[2]}) ${cells[3]}  ${cells[4]}  " +
                    "${cells[5]}  ${cells[6]}  ${AppLocale.str(R.string.report_col_note)}: " +
                    cells[7].ifBlank { AppLocale.str(R.string.report_none) },
            )
        }
    }

    // ---------- Excel (CSV, 带 BOM 写出) ----------

    private fun renderCsv(entries: List<TimeLogEntity>): String = buildString {
        appendLine(
            listOf(
                R.string.report_col_year,
                R.string.report_col_md,
                R.string.report_col_weekday,
                R.string.report_col_category,
                R.string.report_col_event,
                R.string.report_col_start,
                R.string.report_col_end,
                R.string.report_col_minutes,
                R.string.report_col_duration,
                R.string.report_col_note,
            ).joinToString(",") { AppLocale.str(it) },
        )
        entries.forEach { e ->
            val day = LocalDate.parse(e.date, TimeRules.DATE)
            val cells = listOf(
                e.year.toString(),
                AppLocale.str(R.string.report_md_cell, day.monthValue, day.dayOfMonth),
                weekday(day),
                AppLocale.str(R.string.report_cat_cell, TimeRules.roman(e.category), e.category),
                e.event,
                e.startTime,
                e.endTime,
                e.durationMinutes.toString(),
                TimeRules.formatMinutes(e.durationMinutes),
                e.note,
            )
            appendLine(cells.joinToString(",") { escapeCsv(it) })
        }
    }

    private fun escapeCsv(v: String): String =
        if (v.any { it == ',' || it == '"' || it == '\n' }) "\"" + v.replace("\"", "\"\"") + "\"" else v

    // ---------- HTML：商务模板 + 内嵌 SVG 雷达 ----------

    private fun renderHtml(
        from: String, to: String,
        entries: List<TimeLogEntity>,
        categories: List<ExportCategorySummary>,
        total: Int,
    ): String = buildString {
        val title = AppLocale.str(R.string.report_title)
        appendLine("<!DOCTYPE html><html lang=\"${AppLocale.str(R.string.report_locale)}\"><head><meta charset=\"utf-8\">")
        appendLine("<title>$title $from ～ $to</title>")
        appendLine(
            """
            <style>
            body{font-family:"Segoe UI","Microsoft YaHei",sans-serif;margin:0;background:#f4f6f9;color:#1f2a3d}
            .page{max-width:900px;margin:24px auto;background:#fff;box-shadow:0 2px 12px rgba(0,0,0,.08)}
            header{background:#1f3a5f;color:#fff;padding:28px 40px}
            header h1{margin:0 0 6px;font-size:24px} header p{margin:0;color:#c9d6e8;font-size:13px}
            section{padding:24px 40px;border-bottom:1px solid #e6ebf2}
            h2{font-size:17px;color:#1f3a5f;border-left:4px solid #1f3a5f;padding-left:10px;margin:0 0 14px}
            table{border-collapse:collapse;width:100%;font-size:13px}
            th{background:#eef2f7;color:#1f3a5f;text-align:left;padding:7px 10px;border:1px solid #d7dfe9}
            td{padding:6px 10px;border:1px solid #e2e8f0}
            tr:nth-child(even) td{background:#f8fafc}
            .num{text-align:right;font-variant-numeric:tabular-nums}
            .total{font-weight:700}
            .radarbox{display:flex;flex-wrap:wrap;gap:24px;align-items:flex-start}
            footer{padding:16px 40px;color:#8b98aa;font-size:12px}
            </style></head><body><div class="page">
            """.trimIndent()
        )
        appendLine(
            "<header><h1>$title</h1><p>${AppLocale.str(R.string.report_range)} $from ～ $to · " +
                "${AppLocale.str(R.string.report_entries_count, entries.size)} · " +
                "${AppLocale.str(R.string.report_total)} ${TimeRules.formatMinutes(total)}</p></header>",
        )

        appendLine(
            "<section><h2>${AppLocale.str(R.string.report_summary)}</h2><table><tr>" +
                "<th>${AppLocale.str(R.string.report_col_category)}</th>" +
                "<th class=\"num\">${AppLocale.str(R.string.report_total)}</th></tr>",
        )
        appendLine("<tr class=\"total\"><td>${AppLocale.str(R.string.report_total)}</td><td class=\"num\">${TimeRules.formatMinutes(total)}</td></tr>")
        categories.forEach { c ->
            appendLine("<tr><td>${esc(c.name)}</td><td class=\"num\">${TimeRules.formatMinutes(c.totalMinutes)}</td></tr>")
        }
        appendLine("</table></section>")

        categories.forEach { c ->
            if (c.events.isNotEmpty()) {
                appendLine(
                    "<section><h2>${esc(c.name)} · ${AppLocale.str(R.string.report_col_event)}</h2><table><tr>" +
                        "<th>${AppLocale.str(R.string.report_col_event)}</th>" +
                        "<th class=\"num\">${AppLocale.str(R.string.report_col_duration)}</th>" +
                        "<th class=\"num\">${AppLocale.str(R.string.report_col_share)}</th></tr>",
                )
                c.events.forEach { (name, m) ->
                    val pct = if (total > 0) m * 100.0 / total else 0.0
                    appendLine("<tr><td>${esc(name)}</td><td class=\"num\">${TimeRules.formatMinutes(m)}</td><td class=\"num\">%.1f%%</td></tr>".format(pct))
                }
                appendLine("</table></section>")
            }
        }

        appendLine("<section><h2>${AppLocale.str(R.string.report_radar)}</h2><div class=\"radarbox\">")
        categories.forEach { c -> appendLine(radarSvg(c)) }
        appendLine("</div></section>")

        appendLine("<section><h2>${AppLocale.str(R.string.report_entries)}</h2><table><tr>")
        listOf(
            R.string.report_col_year,
            R.string.report_col_md,
            R.string.report_col_weekday,
            R.string.report_col_category,
            R.string.report_col_event,
            R.string.report_col_span,
            R.string.report_col_duration,
            R.string.report_col_note,
        ).forEach { append("<th>${AppLocale.str(it)}</th>") }
        appendLine("</tr>")
        entries.forEach { e ->
            appendLine("<tr>" + entryCells(e).joinToString("") { "<td>${esc(it)}</td>" } + "</tr>")
        }
        appendLine("</table></section>")
        appendLine("<footer>${AppLocale.str(R.string.report_footer)}</footer></div></body></html>")
    }

    private fun esc(v: String): String =
        v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    /** 与 App 内手绘雷达同构：同心环 + 各事件轴多边形 */
    private fun radarSvg(c: ExportCategorySummary): String {
        val axes = CategoryStore.eventsFor(c.name).ifEmpty { c.events.map { it.first } }
        if (axes.isEmpty()) return ""
        val values = axes.map { a -> c.events.firstOrNull { it.first == a }?.second ?: 0 }
        val scale = maxOf(60, TimeRules.radarScale(values.maxOrNull() ?: 0))
        val cx = 170.0
        val cy = 150.0
        val r = 100.0
        return buildString {
            appendLine("<svg width=\"340\" height=\"300\" viewBox=\"0 0 340 300\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("<text x=\"170\" y=\"20\" text-anchor=\"middle\" font-size=\"14\" fill=\"#1f3a5f\" font-weight=\"bold\">${esc(c.name)}</text>")
            for (ring in 1..4) {
                appendLine("<polygon points=\"${ringPoints(cx, cy, r, axes.size, r * ring / 4)}\" fill=\"none\" stroke=\"#c8d2de\" stroke-width=\"1\"/>")
            }
            axes.forEachIndexed { i, a ->
                val (x, y) = point(cx, cy, r, angle(i, axes.size))
                appendLine("<line x1=\"$cx\" y1=\"$cy\" x2=\"${f(x)}\" y2=\"${f(y)}\" stroke=\"#c8d2de\"/>")
                val (lx, ly) = point(cx, cy, r + 16, angle(i, axes.size))
                appendLine("<text x=\"${f(lx)}\" y=\"${f(ly + 4)}\" text-anchor=\"middle\" font-size=\"11\" fill=\"#42546e\">${esc(a)}</text>")
            }
            val dataPts = values.indices.joinToString(" ") { i ->
                val (x, y) = point(cx, cy, r * (values[i] / scale.toDouble()).coerceAtMost(1.0), angle(i, values.size))
                "${f(x)},${f(y)}"
            }
            appendLine("<polygon points=\"$dataPts\" fill=\"rgba(31,58,95,.25)\" stroke=\"#1f3a5f\" stroke-width=\"2\"/>")
            appendLine("</svg>")
        }
    }

    private fun angle(i: Int, n: Int): Double = -Math.PI / 2 + 2 * Math.PI * i / n

    private fun ringPoints(cx: Double, cy: Double, r: Double, n: Int, ringR: Double): String =
        (0 until n).joinToString(" ") { i ->
            val (x, y) = point(cx, cy, ringR, angle(i, n))
            "${f(x)},${f(y)}"
        }

    private fun point(cx: Double, cy: Double, r: Double, a: Double): Pair<Double, Double> =
        (cx + r * cos(a)) to (cy + r * sin(a))

    private fun f(v: Double): String = String.format("%.1f", v)
}
