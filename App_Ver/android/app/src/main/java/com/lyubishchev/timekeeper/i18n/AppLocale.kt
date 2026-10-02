package com.lyubishchev.timekeeper.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 可选语言；SYSTEM 表示跟随系统。tag 与 res/values-<tag> 目录一一对应。 */
enum class AppLanguage(val tag: String) {
    SYSTEM("system"),
    ZH("zh"),
    EN("en"),
    ES("es"),
    PT("pt"),
    DE("de"),
    NL("nl"),
    JA("ja"),
    ;

    val locale: Locale get() = Locale(tag)
}

/**
 * 应用内多语言：设置里选的语言优先；选「跟随系统」时，系统语言若在我们内置的七种里就用它，
 * 否则退回英文。用户自己填的分类名、事件名、备注属于数据，一律不翻译。
 * In-app locale: explicit choice wins; otherwise the system language when we ship it, else English.
 */
object AppLocale {

    private const val FILE = "language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    /** 用户在设置里选的那一档 */
    var choice: AppLanguage = AppLanguage.SYSTEM
        private set

    /** 真正生效的语言 */
    var language: AppLanguage = AppLanguage.ZH
        private set

    var locale: Locale = Locale("zh")
        private set

    /** 本地化资源入口：非 Compose 代码（ViewModel、导出器、领域层）从这里取文案 */
    lateinit var context: Context
        private set

    fun init(appContext: Context) {
        choice = prefs(appContext).getString(KEY_LANGUAGE, null)
            ?.let { stored -> AppLanguage.values().firstOrNull { it.tag == stored } }
            ?: AppLanguage.SYSTEM
        refresh(appContext)
    }

    fun setLanguage(appContext: Context, chosen: AppLanguage) {
        choice = chosen
        prefs(appContext).edit().putString(KEY_LANGUAGE, chosen.tag).apply()
        refresh(appContext)
    }

    /** 重算生效语言并重建本地化 Context；启动时和切换语言后各跑一次 */
    fun refresh(appContext: Context) {
        val base = appContext.applicationContext
        language = if (choice != AppLanguage.SYSTEM) choice else fromSystem()
        locale = language.locale
        // 注意别写成 apply { setLocale(locale) }：那里的 locale 会解析到 Configuration.locale 字段，等于没换。
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        context = base.createConfigurationContext(config)
    }

    fun str(@StringRes id: Int): String = context.getString(id)

    fun str(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    /** 模板存在 strings.xml 里，配合当前语言取 DateTimeFormatter */
    fun dateFormatter(@StringRes patternId: Int): DateTimeFormatter =
        DateTimeFormatter.ofPattern(str(patternId), locale)

    val isChinese: Boolean get() = language == AppLanguage.ZH

    private fun fromSystem(): AppLanguage {
        val code = Locale.getDefault().language
        return AppLanguage.values().firstOrNull { it != AppLanguage.SYSTEM && it.tag == code }
            ?: AppLanguage.EN
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
