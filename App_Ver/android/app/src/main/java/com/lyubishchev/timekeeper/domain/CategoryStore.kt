package com.lyubishchev.timekeeper.domain

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** 一级分类节点：名称 + 二级事件列表 */
data class TimeCategory(
    val name: String,
    val events: List<TimeEvent>,
)

/** 二级事件节点：名称 + 三级子类列表 */
data class TimeEvent(
    val name: String,
    val subs: List<String>,
)

/** 一条"常用"：一级分类 + 二级事件 + 固定时长（分钟） */
data class QuickPick(val category: String, val event: String, val minutes: Int)

/**
 * 时间分类树与常用配置的唯一种子源，SharedPreferences + JSON 持久化。
 * The single source of truth for the (up to three-level) category tree and
 * the quick-pick list. Statistics match by NAME: a renamed node simply stops
 * accumulating old rows, an existing name keeps summing its history.
 */
object CategoryStore {

    const val MAX_CHILDREN = 8

    private const val FILE = "category_store"
    private const val KEY_CATS = "categories"
    private const val KEY_PICKS = "quick_picks"

    private val DEFAULT_CATEGORIES = listOf(
        TimeCategory(
            TimeRules.CATEGORY_L1,
            listOf("健康", "学习", "阅读", "产出", "投资", "社交").map { TimeEvent(it, emptyList()) },
        ),
        TimeCategory(
            TimeRules.CATEGORY_L2,
            listOf("思考", "整理", "兴趣").map { TimeEvent(it, emptyList()) },
        ),
    )

    private val DEFAULT_PICKS = listOf(
        QuickPick(TimeRules.CATEGORY_L1, "阅读", 30),
        QuickPick(TimeRules.CATEGORY_L1, "学习", 60),
        QuickPick(TimeRules.CATEGORY_L1, "产出", 120),
        QuickPick(TimeRules.CATEGORY_L1, "健康", 60),
        QuickPick(TimeRules.CATEGORY_L2, "思考", 30),
    )

    var categories by mutableStateOf(DEFAULT_CATEGORIES)
        private set

    var quickPicks by mutableStateOf(DEFAULT_PICKS)
        private set

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        categories = read(KEY_CATS, ::parseCategories) ?: DEFAULT_CATEGORIES
        quickPicks = read(KEY_PICKS, ::parsePicks) ?: DEFAULT_PICKS
    }

    // ---------- 查询 ----------

    fun categoryNames(): List<String> = categories.map { it.name }

    /** 第 index 个一级分类的名字，越界回退到默认名 */
    fun nameAt(index: Int): String =
        categories.getOrNull(index)?.name
            ?: listOf(TimeRules.CATEGORY_L1, TimeRules.CATEGORY_L2).getOrNull(index)
            ?: TimeRules.CATEGORY_L1

    fun eventsFor(category: String): List<String> =
        categories.firstOrNull { it.name == category }?.events?.map { it.name } ?: emptyList()

    fun eventOrder(category: String, event: String): Int =
        eventsFor(category).indexOf(event).let { if (it < 0) 99 else it }

    private val ROMANS = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII")

    /** 类别用序号罗马数字表示，未知类别按 I */
    fun roman(category: String): String {
        val index = categories.indexOfFirst { it.name == category }.coerceAtLeast(0)
        return ROMANS.getOrNull(index) ?: "I"
    }

    // ---------- 变更（全部按索引定位，界面里拿的就是索引） ----------

    fun addCategory(name: String) = mutate(categories + TimeCategory(name, emptyList()))

    fun renameCategory(ci: Int, name: String) =
        mutate(categories.setAt(ci) { it.copy(name = name) })

    fun deleteCategory(ci: Int) = mutate(categories.removedAt(ci))

    fun addEvent(ci: Int, name: String) =
        mutate(categories.setAt(ci) { c -> c.copy(events = c.events + TimeEvent(name, emptyList())) })

    fun renameEvent(ci: Int, ei: Int, name: String) =
        mutate(categories.setAt(ci) { c -> c.copy(events = c.events.setAt(ei) { it.copy(name = name) }) })

    fun deleteEvent(ci: Int, ei: Int) =
        mutate(categories.setAt(ci) { c -> c.copy(events = c.events.removedAt(ei)) })

    fun addSub(ci: Int, ei: Int, name: String) =
        mutate(categories.setAt(ci) { c ->
            c.copy(events = c.events.setAt(ei) { e -> e.copy(subs = e.subs + name) })
        })

    fun renameSub(ci: Int, ei: Int, si: Int, name: String) =
        mutate(categories.setAt(ci) { c ->
            c.copy(events = c.events.setAt(ei) { e -> e.copy(subs = e.subs.setAt(si) { name }) })
        })

    fun deleteSub(ci: Int, ei: Int, si: Int) =
        mutate(categories.setAt(ci) { c ->
            c.copy(events = c.events.setAt(ei) { e -> e.copy(subs = e.subs.removedAt(si)) })
        })

    fun addPick(category: String, event: String, minutes: Int) =
        mutatePicks(quickPicks + QuickPick(category, event, minutes))

    fun deletePick(pi: Int) = mutatePicks(quickPicks.removedAt(pi))

    // ---------- 持久化 ----------

    private fun mutate(next: List<TimeCategory>) {
        categories = next
        write(KEY_CATS, categoriesJson(next))
    }

    private fun mutatePicks(next: List<QuickPick>) {
        quickPicks = next
        write(KEY_PICKS, picksJson(next))
    }

    private fun write(key: String, json: String) {
        prefs?.edit()?.putString(key, json)?.apply()
    }

    private fun <T> read(key: String, parser: (String) -> T?): T? =
        prefs?.getString(key, null)?.let { runCatching { parser(it) }.getOrNull() }

    private fun categoriesJson(list: List<TimeCategory>): String = JSONArray().run {
        list.forEach { cat ->
            put(
                JSONObject()
                    .put("name", cat.name)
                    .put("events", JSONArray().apply {
                        cat.events.forEach { e ->
                            put(
                                JSONObject()
                                    .put("name", e.name)
                                    .put("subs", JSONArray().apply { e.subs.forEach { put(it) } }),
                            )
                        }
                    }),
            )
        }
    }.toString()

    private fun picksJson(list: List<QuickPick>): String = JSONArray().run {
        list.forEach { put(JSONObject().put("c", it.category).put("e", it.event).put("m", it.minutes)) }
    }.toString()

    private fun parseCategories(text: String): List<TimeCategory>? {
        val arr = JSONArray(text)
        if (arr.length() == 0) return null
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val events = JSONArray(o.optString("events", "[]"))
                add(
                    TimeCategory(
                        name = o.getString("name"),
                        events = buildList {
                            for (j in 0 until events.length()) {
                                val e = events.getJSONObject(j)
                                val subs = JSONArray(e.optString("subs", "[]"))
                                add(
                                    TimeEvent(
                                        name = e.getString("name"),
                                        subs = buildList {
                                            for (k in 0 until subs.length()) add(subs.getString(k))
                                        },
                                    ),
                                )
                            }
                        },
                    ),
                )
            }
        }
    }

    private fun parsePicks(text: String): List<QuickPick>? {
        val arr = JSONArray(text)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(QuickPick(o.getString("c"), o.getString("e"), o.getInt("m")))
            }
        }
    }
}

private fun <T> List<T>.removedAt(index: Int): List<T> =
    filterIndexed { i, _ -> i != index }

private fun <T> List<T>.setAt(index: Int, transform: (T) -> T): List<T> =
    if (index !in indices) this else mapIndexed { i, item -> if (i == index) transform(item) else item }
