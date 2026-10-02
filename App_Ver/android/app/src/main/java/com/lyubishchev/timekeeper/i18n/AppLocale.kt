package com.lyubishchev.timekeeper.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 可选语言；SYSTEM 表示跟随系统。tag 与 res/values-<tag> 目录一一对应，
 * 且 tag 是 SharedPreferences 里的存量值，一旦写下不可更改。
 * locale 逐档自带：ZH_TW 的 tag「zh-rTW」不能直接喂给 Locale 单参构造（那是错的），
 * 必须用 Locale("zh", "TW")，才能命中 values-zh-rTW。
 */
enum class AppLanguage(val tag: String, val locale: Locale) {
    SYSTEM("system", Locale("zh")), // locale 占位：SYSTEM 永不成为生效语言（见 refresh）
    ZH("zh", Locale("zh")),
    ZH_TW("zh-rTW", Locale("zh", "TW")),
    EN("en", Locale("en")),
    ES("es", Locale("es")),
    PT("pt", Locale("pt")),
    DE("de", Locale("de")),
    NL("nl", Locale("nl")),
    JA("ja", Locale("ja")),
    ;
}

/**
 * 应用内多语言：设置里选的语言优先；选「跟随系统」时，系统语言若在我们内置的八种里就用它，
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

    /**
     * 仅指简体中文。繁体中文不算「中文」是有意的：VersionHistoryScreen 用它决定
     * 直接显示硬编码简体的 AppVersions.history，还是走 release_notes_* 本地化资源；
     * 繁体用户必须走资源路径，才能拿到 values-zh-rTW 里的繁体 release_notes。
     */
    val isChinese: Boolean get() = language == AppLanguage.ZH

    private fun fromSystem(): AppLanguage {
        val sys = Locale.getDefault()
        if (sys.language == "zh") {
            // 繁体系统（Hant 脚本或台/港/澳地区）落 ZH_TW，其余中文仍落 ZH（简体）
            val traditional = sys.script.contains("Hant") || sys.country in setOf("TW", "HK", "MO")
            return if (traditional) AppLanguage.ZH_TW else AppLanguage.ZH
        }
        val code = sys.language
        return AppLanguage.values().firstOrNull { it != AppLanguage.SYSTEM && it != AppLanguage.ZH_TW && it.tag == code }
            ?: AppLanguage.EN
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
