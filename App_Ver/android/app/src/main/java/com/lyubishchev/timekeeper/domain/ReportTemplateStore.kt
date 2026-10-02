package com.lyubishchev.timekeeper.domain

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lyubishchev.timekeeper.R

/** 内置报告模板：默认 Markdown，TXT 只是同内容的纯文本载体 */
enum class BuiltinTemplate(val id: String, @StringRes val labelRes: Int, @StringRes val descRes: Int) {
    MARKDOWN("builtin:markdown", R.string.tpl_builtin_md, R.string.tpl_builtin_md_desc),
    TEXT("builtin:text", R.string.tpl_builtin_txt, R.string.tpl_builtin_txt_desc),
}

/** 用户上传的一个模板文件；本版只登记，解析留给后续版本 */
data class CustomTemplate(val name: String, val uri: String)

/**
 * 报告模板的极简存储：选中项 + 自定义模板清单，落盘用 SharedPreferences。
 * 自定义条目按 "名称\tURI" 一行一条存放，显示名里不会有制表符或换行。
 */
object ReportTemplateStore {

    private const val FILE = "report_template_prefs"
    private const val KEY_SELECTED = "selected_template"
    private const val KEY_CUSTOM = "custom_templates"

    var selectedId by mutableStateOf(BuiltinTemplate.MARKDOWN.id)
        private set

    var custom by mutableStateOf<List<CustomTemplate>>(emptyList())
        private set

    fun init(context: Context) {
        custom = decode(prefs(context).getString(KEY_CUSTOM, null))
        selectedId = prefs(context).getString(KEY_SELECTED, null)
            ?.takeIf { stored -> knownIds().contains(stored) }
            ?: BuiltinTemplate.MARKDOWN.id
    }

    fun select(context: Context, id: String) {
        selectedId = id
        prefs(context).edit().putString(KEY_SELECTED, id).apply()
    }

    fun add(context: Context, template: CustomTemplate) {
        if (custom.any { it.uri == template.uri }) return
        saveCustom(context, custom + template)
    }

    fun remove(context: Context, uri: String) {
        saveCustom(context, custom.filterNot { it.uri == uri })
        if (selectedId == uri) select(context, BuiltinTemplate.MARKDOWN.id)
    }

    private fun knownIds(): Set<String> =
        BuiltinTemplate.entries.map { it.id }.toSet() + custom.map { it.uri }

    private fun saveCustom(context: Context, list: List<CustomTemplate>) {
        custom = list
        val raw = list.joinToString("\n") { "${it.name}\t${it.uri}" }
        prefs(context).edit().putString(KEY_CUSTOM, raw).apply()
    }

    private fun decode(raw: String?): List<CustomTemplate> =
        raw?.lineSequence()?.mapNotNull { line ->
            val name = line.substringBefore('\t')
            val uri = line.substringAfter('\t', "")
            if (name.isBlank() || uri.isBlank()) null else CustomTemplate(name, uri)
        }?.toList() ?: emptyList()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
