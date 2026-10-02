# 柳比歇夫时间日志 · Lyubishchev Time-Log

> 一个把「柳比歇夫时间记录法」落地的个人项目，同一套理念先后有三种实现：**移动端 App**、**Python 数据处理**、**Web 网站**。
> A personal project implementing the *Lyubishchev time-tracking method*, built three times over: a **mobile App**, a **Python data-processing** tool, and a **Web** site.

## 三种版本一览 · Three Versions at a Glance

| 版本 / Version | 状态 / Status | 说明 / Note |
| --- | --- | --- |
| **App 版**（`App_Ver/`） | 🟢 **主力 · 当前在用** / **Primary, actively developed** | 原生 Android、纯离线、随身记录，界面支持八种语言。v0.93。Native Android, fully offline, record on the go, with an eight-language UI. v0.93. |
| **Web 版**（`Web_Ver/`） | 🟡 **自用 · 尚未开发完** / **Personal use, work-in-progress** | Vue 3 网站，仅供个人分析使用，功能仍在补齐。Vue 3 site for personal analysis, not feature-complete. |
| **Python 版**（`PY_Ver/`） | 🔴 **已归档 · 不再维护** / **Archived, no longer maintained** | 2021 年归档的配置驱动报告工具，仅作历史参考。Config-driven report tool, archived in 2021, kept for reference only. |

> 一句话：**日常记录用 App，深度分析偶尔用 Web，Python 已经退役。**
> In short: **log daily with the App, occasionally analyze on the Web, Python has retired.**

---

# 一、App 版（主力） · The App (Primary)

原生安卓应用，采用 **Jetpack Compose + Material 3 + Room**，**纯离线**——数据只存在手机本地，无需联网、无需账号。当前版本 **0.93**。
A native Android app built with **Jetpack Compose + Material 3 + Room**, **fully offline** — data lives only on the phone, no network or account required. Current version **0.93**.

## 1.1 程序结构（代码是怎么摆的） · Program Structure (how the code is laid out)

代码位于 `App_Ver/android/app/src/main/java/com/lyubishchev/timekeeper/`，按职责分为四层：
Source lives under `App_Ver/android/app/src/main/java/com/lyubishchev/timekeeper/`, split into four layers by responsibility:

```
timekeeper/
├── MainActivity.kt / TimekeeperApp.kt     应用与 Activity 入口（含语言覆盖）/ app & activity entry (incl. locale override)
├── data/       数据层 · Data layer
│   ├── AppDatabase.kt          Room 数据库单例（timekeeper.db）/ Room DB singleton
│   ├── TimeLogEntity.kt        一条时间记录 → time_logs 表 / one log row
│   ├── TimeLogDao.kt           增删改查与各类统计查询 / CRUD + aggregate queries
│   └── TimeLogRepository.kt    唯一数据出入口，写入时算好时长/周/年 / single gateway, derives duration/week/year
├── domain/     领域层 · Domain layer
│   ├── TimeRules.kt            时长、ISO 周、雷达刻度等规则 / time rules
│   ├── CategoryStore.kt        分类与事件的树（可维护）/ category & event tree
│   ├── AppMeta.kt              首次运行日起算的陪伴天数 / days-since-first-run counter
│   ├── ReportTemplateStore.kt  报告模板：内置与上传文件的登记 / registered report templates
│   └── ExportRenderer.kt       MD/TXT/Excel/HTML 报告渲染 / report rendering
├── i18n/       AppLocale.kt    八语目录与 Locale 切换 / eight-language locale switching
└── ui/         界面层 · UI layer
    ├── AppRoot / AppBottomBar / AppTab / SplashGate   底部五入口外壳、切换与启动屏 / shell, 5-slot navigation, splash
    ├── screen/  各页面 / screens：Home · Log · AddRecord · Mine · CategoryEditor · QuickPickEditor · ReportTemplate · VersionHistory …
    ├── export/  按需导出 / 全量导入界面与 ViewModel
    ├── widget/  RadarChart 雷达图、ChoiceRow 等复用组件 / reusable widgets
    └── theme/   颜色模式、主题配色（控件色 + 与主题色相配套的中性底色）、字体排印 / color mode, theme palettes (accents + hue-matched neutral layers), typography
```

技术栈：Kotlin、Compose (Material 3)、Room + KSP、Java 17、`minSdk 26`。
Stack: Kotlin, Compose (Material 3), Room + KSP, Java 17, `minSdk 26`.

## 1.2 逻辑结构（这玩意怎么用） · Logic Structure (how it works & how you use it)

底部是**五个一级入口**（中间是抖音式的「记一笔」快捷按钮）：
Five top-level destinations in the bottom bar (the center slot is a TikTok-style "log now" button):

| 入口 | Tab | 用途 / Purpose |
| --- | --- | --- |
| 概览 | **Home** | 今日 / 本周 / 本月累计（这三块统计的时长用紧凑的 h/m 记法，如 `207h 50m`，单位记号不随语言变），一张雷达图做「今天 vs 昨天」「本周 vs 上周」「本月 vs 上月」的对比，两条系列靠「明暗＋线型」区分——当期是主色实线带圆点、对比期是同一主色往卡片底色退一档的虚线 / today, this-week, this-month totals (these three blocks use a compact h/m form such as `207h 50m`, whose unit symbols stay untranslated) with a radar comparing day/week/month vs the previous one, its two series told apart by shade + line style — the current period a solid primary line with point markers, the comparison period a dashed line in the same primary pulled toward the card surface |
| 记录 | **Log** | 按 **日 / 周 / 月 / 年** 四档浏览历史记录，手风琴式展开每条明细；年档卡片头部常驻年份标题、年度总计与一类/二类合计且整行可点，其下逐条事件行点击头部才展开 / browse history by day / week / month / year with accordion detail; in the year view the year title, yearly total and Category I/II totals stay pinned in the tappable card header, while the per-event lines beneath expand on tap |
| 记一笔 | **Add** | 登记一条时间；「开始」「结束」并排成同一行、各占半宽（时间字号由 `headlineSmall` 收到 `titleMedium`、半粗且走主色，八种语言下靠单行限制＋省略号保证不换行，点选区保留加宽的竖向内边距以整块可点），未选的时间与未算出的时长不再写本地化的「待填写」，统一显示语言无关的下划线 `____` 占位；开始时间自动承接上一条最新结束时间，时长按起止自动算；结束不晚于开始时不接受该值、保留原值并弹出既有的「结束时间需晚于开始时间」提示横幅（开始时间顶掉已有结束时间时同样会给提示）；可用「快捷设置」一键套用常用组合，由开始时间推算出的结束时间最晚 23:59，跨午夜一律夹到 23:59、时长只记当天剩余分钟 / log an entry; start and end now sit side by side on one row at half width each (the time text drops from `headlineSmall` to `titleMedium`, semibold in the primary color, held to a single line with ellipsis so it never wraps in any of the eight languages, with widened vertical padding keeping the whole value tappable), and an unset time or undetermined duration no longer reads "pending" in the local language but shows a language-neutral underscore `____` placeholder; start time auto-follows the latest end; duration auto-computed; an end time that is not after the start is not accepted — the previous value is kept and the existing "end time must be later than start time" banner explains why, and a start pushed past an existing end is reported the same way; quick-pick presets for one-tap entry, whose derived end time never passes 23:59 — a cross-midnight pick is clamped there and counts only the minutes left in the day |
| 报表 | **Report** | ⚠️ 规划中的占位页 / placeholder — planned, not yet implemented |
| 设置 | **Mine** | 时间分类、快捷设置、按需导出、全量导入/导出、报告模板、AI API（占位）、颜色模式、主题配色、语言、版本历史 / categories, quick-picks, on-demand export, full import/export, report templates, AI API (placeholder), color mode, themes, language, version history |

核心数据模型是一条 `time_logs` 记录：**日期 + 开始/结束时间 + 时长 + 类别 + 事件 + 备注**。类别固定为 **I 类时间**（维持生存与成长）与 **II 类时间**（为时间本身服务的时间），事件是可新建、改名、删除的两层结构（每类最多 8 个）。`date`、时间以文本存储，`week_number` / `year` 在写入时算好，让周、年统计走索引。
The core model is one `time_logs` row: **date + start/end time + duration + category + event + note**. Categories are fixed as **Class-I** (sustenance & growth) and **Class-II** (upkeep of the system); events are a two-level list you can add/rename/delete (max 8 per category). Dates/times are stored as text while `week_number` / `year` are denormalized at write time so week/year stats hit indexes.

导出与同步：**按需导出**按日期区间生成含总计、分类逐事件汇总、雷达图与逐条明细的报告，支持 MD / TXT / Excel / HTML；**全量导出**可一键导出 SQLite 数据库文件（.db）或 CSV；**全量导入**读取本 App 导出的 .db / CSV 逐条比对合并，同日期、同起止、同事件、同时长视为重复自动跳过、不双计。
Export & sync: **on-demand export** produces a report for a date range (totals, per-category/event summary, radar, line-by-line detail) in MD / TXT / Excel / HTML; **full export** dumps the whole DB as a SQLite `.db` file or CSV; **full import** reads a `.db` / CSV exported by this app and merges row by row, skipping duplicates (same date, span, event, duration) so nothing is double-counted.

外观可调配：**颜色模式**支持跟随系统 / 浅色 / 深色；**主题**提供十套配色一键整组切换（莫兰迪灰绿 / 蒂芙尼蓝 / 海运蓝 / 森林绿 / 朱砂红 / 琥珀黄 / 蔷薇粉 / 石墨灰 / 曜石黑 / Solarized）。从 0.93 起，每套主题除控件色之外还自带一组与主色相相配的中性底色——页面大背景、卡片与浮起层、文字与描边一起染色，所以设置页的选项卡片、底部五个主菜单那一栏、乃至对话框与下拉菜单都会跟着换色；浅色是**带色相的浅**、深色是**带色相的深**（蔷薇粉浅底 #FBEAF0 透着粉、深底 #1E1217 是暗玫瑰，森林绿浅底 #EDF9F3 泛着绿、深底 #141C18 是墨绿），中性底色不再恒为莫兰迪。
Appearance: **color mode** follows system / light / dark; **themes** offer ten palettes switchable in one tap (Morandi grey-green / Tiffany blue / navy / forest green / crimson / amber / rose / graphite / obsidian / Solarized). From 0.93 each theme also carries a set of neutral tones matched to its own hue — page background, cards and raised surfaces, text and outlines are dyed together, so the option cards on the Settings screen, the five-slot bottom bar, and even dialogs and dropdown menus follow the theme; light is a **tinted light** and dark a **tinted dark** (Rose rests on a pink-tinged #FBEAF0 and a deep plum #1E1217, Forest on a green-tinged #EDF9F3 and a dark pine #141C18), replacing the formerly constant Morandi base.

十套的中性层按三种方式取色：默认那套莫兰迪（暖米白 / 炭灰）保持 0.92 原值一字未改，作为「无主题」基准；蒂芙尼蓝、海运蓝、森林绿、朱砂红、琥珀黄、蔷薇粉、石墨灰七套按各自主色相与自身饱和乘数生成；曜石黑与 Solarized 手工调值（Solarized 用官方羊皮纸 #FDF6E3 系与深蓝黑 #002B36 系）。实测对比度（WCAG 相对亮度比）：生成盘整体正文对底 11.5–15.3、次要文字对底 4.85–7.25 全部达标，其中蔷薇粉浅 11.98 / 深 15.23、森林绿浅 12.00 / 深 14.72、Solarized 浅 11.75 / 深 12.25；石墨灰浅底 #F2F3F3 近乎中性以守住「沉稳」定位，琥珀黄浅底 #FCF8EA。既有短板（非本轮引入、本轮按要求未动）：莫兰迪浅色的次要文字对底约 4.16:1，低于 4.5。
The ten neutral layers are sourced three ways: the default Morandi set (warm off-white / charcoal) keeps its 0.92 values untouched and acts as the "no theme" baseline; Tiffany blue, navy, forest green, crimson, amber, rose and graphite are generated from each palette's own hue and saturation multiplier; obsidian and Solarized are tuned by hand (Solarized with its official parchment #FDF6E3 and deep blue-black #002B36 families). Measured contrast (WCAG relative-luminance ratios): across the generated palettes body text runs 11.5–15.3 against its background and secondary text 4.85–7.25, all passing — Rose 11.98 light / 15.23 dark, Forest 12.00 / 14.72, Solarized 11.75 / 12.25; graphite stays an almost neutral #F2F3F3 in light mode to hold its sober character and amber a #FCF8EA. One pre-existing weakness, left alone this round by request: Morandi light secondary text sits at about 4.16:1, below 4.5.

主题染色那一轮没有动界面布局——染色只发生在 `ui/theme/` 的色盘定义里（版本历史那一页只多了新条目），全项目本来就只取 `MaterialTheme.colorScheme` 的角色色、零硬编码色值。仍未同步的只剩 App 启动首帧之前的窗口底色：它取自固定的启动色（浅色 #F7F4EE / 深色 #201E1A），因此换主题的那一瞬间仍是莫兰迪底，动态窗口底色留待后续版本。
The tinting round changed no screen layout — the tinting lives entirely in the palette definitions under `ui/theme/` (the version-history screen only gained the new entry) — since the project already reads only `MaterialTheme.colorScheme` roles with zero hard-coded colors. What remains unsynced is the window colour shown before the app's first frame: it still comes from the fixed launch color (light #F7F4EE / dark #201E1A), so the instant of a theme switch stays on a Morandi base until a dynamic window background lands in a later release.

多语种与启动屏：界面支持**简体中文 / 繁體中文 / English / Español / Português / Deutsch / Nederlands / 日本語**八种语言，可在「设置 → 语言」随时切换（简体中文与繁體中文是并列的两档），选「跟随系统」时按手机系统语言自动适配，中文系统里脚本或地区属繁体（Hant／TW／HK／MO）的落繁體中文、其余中文仍落简体；应用内外的文本（导出报告的正文、表头、星期、时长单位、固定的两个一级分类「I类时间 / II类时间」，乃至整份版本历史——繁体读者拿到的是繁体译文，简体读者仍直读原文）都随语言走，计数句（陪伴天数、记录条数）还按各语言的单复数规则变化，**但你自己填写的事件、快捷条目与备注属于个人数据，一律不翻译**。启动时先经过 Splash 页：居中图标、按语言显示的应用名、「已陪伴您 N 天」（自首次运行起算）与 *LOOYEA* 出品署名。
Languages & splash: the UI ships in **eight languages — 简体中文 / 繁體中文 / English / Español / Português / Deutsch / Nederlands / 日本語**, switchable under Settings → Language, where Simplified and Traditional Chinese sit side by side as two separate choices, with a "follow the system" option that resolves a Chinese system locale written in Traditional (Hant script, or a TW/HK/MO region) to Traditional while other Chinese locales stay Simplified; interface text, exported reports, the two fixed top-level classes (Class I / Class II time) and the whole in-app changelog follow the language — Traditional readers get the translated changelog while Simplified readers still read the original text — and count lines such as days together and entries logged follow each language's singular/plural rules, **while events, quick picks and notes you wrote are personal data and are never translated**. Launching shows a splash screen: centered icon, localized app name, "days together since first launch" and the italic *LOOYEA* credit.

报告模板与 AI API：「设置 → 报告模板」可选用内置 Markdown 或纯文本模板，也能上传自己的 `.md` / `.txt`（文本内容需按 Markdown 书写）；本版只做登记与选用，模板解析留待后续。「AI API」目前只是入口占位，用于将来让报告自选推理引擎与调用方式，不进行任何联网配置。
Report templates & AI API: Settings → Report templates lets you pick the built-in Markdown or plain-text template, or upload your own `.md` / `.txt` (whose content must still be written in Markdown); this release only registers and selects them — parsing comes later. The **AI API** row is a placeholder entry for a future choice of reasoning engine / API method, with no network configuration at all.

## 1.3 版本进化 · Version History

App 内「设置 → 版本」记录了完整更新历史（当前 **v0.93**，且这份历史本身也翻了八种语言）。几个里程碑：
The in-app **Settings → Version** screen keeps the full changelog (currently **v0.93**, itself translated into all eight languages). Key milestones:

- **v0.1** 首个真机可用版：概览、记录、记一笔、报表占位、我的五页框架，本地 Room 存储 / first on-device build: the five-page shell, local Room storage.
- **v0.2–0.3** 雷达图对比、日/周/月/年四档浏览、「记一笔」重排、「常用（快捷设置）」上线 / radar comparisons, four-range browsing, reworked log form, quick-pick presets.
- **v0.4–0.5** 导出报告、全量导出 CSV、分类模型简化为固定两类、事件两层 / export reports, full CSV export, categories simplified to fixed two classes with two-level events.
- **v0.6–0.7** 雷达轴跟随数据库、新增「本月」档、全量导出 .db、新增全量导入 / radar axes follow the DB, "this month" range, `.db` full export, full import.
- **v0.8–0.9** 「莫兰迪极简」换装、大号衬线数字、记录页按有记录的周/月/年动态列出 / "Morandi minimal" redesign, large serif numbers, dynamic week/month/year listing.
- **v0.91** 主题正式上线（四套配色一键切换）及若干界面精简 / themes shipped (four switchable palettes) plus UI trimming.
- **v0.92** 七语种界面 + 启动 Splash、主题扩到十套、报告模板页与 AI API 入口、设置页统计卡三行（陪伴天数 / 累计时长 / 记录条数）/ seven-language UI plus splash, themes grown to ten palettes, report-template screen and AI API entry, three-line stats card in Settings.
- **v0.93** 主题补齐配套底色：十套色盘各自带一组按自身主色相染成的中性层（背景 / 表面 / 浮起层 / 文字 / 描边，浅深分开），并补上 Material 3 的表面容器各档位，主题由「只换控件色」变成「控件色 + 底色一起换」，默认莫兰迪维持原样；记录页年视图改为手风琴——年份标题、年度总计与一类/二类合计常驻卡片头部且整行可点，其下逐条子类事件行默认收起、点击头部才展开（周/月手风琴与日档不变）；本轮另新增第八种语言——繁体中文，全文按台湾惯用术语转写，「设置 → 语言」里简体中文与繁體中文并列可选；首页雷达的两条系列改为「明暗＋线型」编码——十套色盘的主/辅色相距离本就偏小、低饱和盘里两条轮廓几乎分不出，故色盘一个色值都不改、零新增颜色，当期用主色实线（2dp）加圆点、填充 0.28，对比期用 `radarPreviousColor` = 同一主色往卡片底色退一档（`lerp(primary, surface, 0.45f)`，浅色模式变浅、深色模式变暗）配 1.5dp 虚线、无圆点、填充 0.12，概览图例与图里同一套编码（实线段+圆点 vs 虚线段）并撤掉 `scheme.secondary`，十套主题一律生效、灰色系（石墨灰/曜石黑）也靠明暗与线型分得开；概览页那三块统计（今日已记录 / 本周累计记录 / 本月累计记录）的时长改用新增的 `TimeRules.formatMinutesCompact` 与新资源 `duration_short_hm/h/m`（八份目录同写法 `%1$dh %2$dm`、`%1$dh`、`%1$dm`，h/m 是不译的国际单位记号），因为原来的「207 小时 50 分钟」在德/西/葡/荷/日语下放不下同一行，而记录页、记一笔、导出与报告正文、设置页统计卡一律保留本地化单位，界面字符串总数由 208 增至 **211**（八份）；本轮随后又改版记一笔页：「开始」「结束」由各占一行并排成一行、各占半宽，时间字号由 `headlineSmall` 收到 `titleMedium`（半粗＋主色），点选区保留加宽的竖向内边距以整块可点，八种语言下靠 `maxLines=1` 加省略号保证不换行，未选时间与未算出的时长不再写「待填写 / 待填写时间」这类本地化文字，统一改为语言无关的下划线 `____`——`add_time_pending`、`add_duration_pending` 两键在八份目录同值，键数不变、界面字符串总数仍是 **211**；「结束时间不能早于开始时间」的落实方式改了：原先非法的结束时间会被静默清空（点了像没反应），现在不接受该值、保留原值，并弹出一条已有的提示横幅说明「结束时间需晚于开始时间」，开始时间往上顶掉已有结束时间时同样会给提示；「结束时间最晚 23:59」——快捷组合（如「学习 1 小时」）从开始时间推算结束时间时，跨午夜一律夹到 23:59、时长记为当天剩余分钟数，不再回绕成次日 `00:00` 被当成合法时长，历史数据与全量导入的 CSV 仍按原有跨午夜解析契约处理，未受影响 / themes gain their own tinted neutral layers — every palette now ships hue-matched background, surfaces, text and outlines for light and dark plus the Material 3 surface-container tiers, so a theme changes the whole page and not just the controls, with the default Morandi left untouched; the Journal's year view becomes an accordion — the year title, yearly total and Category I/II totals stay pinned in the tappable card header while the per-event lines beneath each year start collapsed and expand on tap (week and month accordions and the day view unchanged); an eighth UI language arrives in this round — Traditional Chinese, transcribed throughout with Taiwan terminology, offered in Settings → Language as its own row next to Simplified Chinese; the Home radar's two series are re-encoded by shade + line style — the palettes' primary/secondary hues sit too close for the low-saturation sets to tell the two outlines apart, so no palette value is changed and no color added: the current period keeps a solid 2dp primary line with point markers at 0.28 fill, while the comparison period uses `radarPreviousColor` = the same primary pulled one step toward the card surface (`lerp(primary, surface, 0.45f)`, lighter in light mode, darker in dark mode) as a 1.5dp dashed line with no markers at 0.12 fill, the overview legend switching to the same encoding (solid segment + dot vs dashed segment) with `scheme.secondary` dropped, so all ten themes apply and even the greys (graphite/obsidian) separate by shade and line style; the three overview blocks (logged today / this week / this month) switch to the new `TimeRules.formatMinutesCompact` and the new `duration_short_hm/h/m` resources (identical `%1$dh %2$dm`, `%1$dh`, `%1$dm` across all eight directories, h/m left as untranslated international unit symbols), since the old "207 h 50 min" wording no longer fit one line in German/Spanish/Portuguese/Dutch/Japanese, while the Log screen, Add-record, the export/report body and the Settings stat card keep their localized units, growing the UI string count from 208 to **211** across the eight directories; later in this round the Add-record screen was reworked: start and end move from one-per-row onto a shared row at half width each, with the time text pulled from `headlineSmall` down to `titleMedium` (semibold in the primary color) and the tap area keeping widened vertical padding, while `maxLines=1` plus an ellipsis stops any of the eight languages from wrapping, and an unchosen time or uncomputed duration no longer shows localized "pending" wording but a language-neutral underscore `____` — the `add_time_pending` and `add_duration_pending` keys carry the same value in all eight directories, so the key count is unchanged and the UI string total stays at **211**; the "end time may not precede the start" rule is now enforced differently: instead of silently clearing an illegal end time (so a tap looked like nothing had happened), the value is rejected, the previous one kept, and an already-existing banner explains that the end time must be later than the start, as it now does when a start time pushes an existing end out of the way; and a derived end time tops out at 23:59 — when a quick-pick combination (e.g. "study 1 hour") computes its end from the start, a cross-midnight span is clamped to 23:59 and the duration recorded as the minutes remaining in that day rather than wrapping to `00:00` the next day and being accepted as a valid length, while historical data and full CSV imports keep the original cross-midnight parsing contract untouched.

---

# 二、Web 版（自用 · 未完成） · The Web (Personal, WIP)

基于 **Vue 3 + TypeScript + Vite** 的网站版，用 **Element Plus** 做界面、**ECharts** 画图、**Dexie（IndexedDB）** 在浏览器本地存储数据。定位是**个人深度分析**用的看板，含仪表盘、周/月/分布报告、数据导入、结算归档等页面，但**功能尚未开发完整**，也在持续变动。
A site built on **Vue 3 + TypeScript + Vite**, using **Element Plus** for UI, **ECharts** for charts, and **Dexie (IndexedDB)** for browser-local storage. It serves as a **personal analytics** dashboard — dashboard, week/month/distribution reports, data import, settlement archive — but is **not feature-complete** and still evolving.

```
Web_Ver/src/
├── views/        页面：Dashboard · WeekReport · MonthlyReport · DistReport · DataImport …
├── components/   charts / common / layout / log 组件
├── composables/  useWeekReport · useReportGenerator · useChartTheme
├── stores/       Pinia：timeLog · settlement
├── db/           Dexie 本地库
└── utils/        dataAggregator · dataParser · exportUtils · timeUtils
```

运行 / Run: 需 Node.js 环境，在 `Web_Ver/` 下 `npm install` 后 `npm run dev`（或双击 `start-web.bat`）。
Requires Node.js; run `npm install` then `npm run dev` inside `Web_Ver/` (or use `start-web.bat`).

---

# 三、Python 版（已归档 · 不再维护） · The Python (Archived, Unmaintained)

> ⚠️ **该版本已于 2021 年归档，不再维护**，仅作历史与思路参考。请勿期待新特性或修复。
> ⚠️ **Archived in 2021 and no longer maintained.** Kept only for historical reference; do not expect new features or fixes.

早期用 Python 做的**配置驱动型数据处理与报告**工具：完全依赖配置文件驱动处理流程，把时间日志汇总、绘图、输出为报告。
An early **config-driven data-processing & reporting** tool: the whole pipeline is driven by configuration files, aggregating time logs, drawing charts, and emitting reports.

## 3.1 程序结构 · Program Structure

代码位于 `PY_Ver/`：
Source lives under `PY_Ver/`:

```
PY_Ver/
├── __main__.py          主程序入口 / entry point
├── basecfg.py           配置加载与装配（使用 ctx 上下文）/ config loading & assembly (use the ctx context)
├── config.yaml          主配置 / main config
├── DistReport/          分布报告：DataProcessor / ChartProcessor / OutputProcessor / DistReport.py + yaml
├── MonthlyReport/       月报：同上三处理器 + MonthlyReport.py + yaml
├── Template2020/        2020 模板：global_draw(_range).py、global_static(_range).py
└── testfiles/           开发期的技术验证脚本 / throwaway tech-verification scripts
```

## 3.2 逻辑结构 · Logic Structure

整体是「**加载配置 → 按区块任务组织处理链**」的思路：

1. **加载 ActiveProfiles**：确定当前激活哪些区块（区段名，大小写敏感、逗号分隔）。
2. **区块配置**：每个区块含任务名、任务说明与「配置类处理链」——即 **调用链（数据处理）→ 图表链（绘图）→ 输出链（HTML/Markdown 等）**。
3. **任务处理链**：每条链最多三段（以逗号分隔），分别对应数据处理、图表绘制、数据输出，尽量保持任务简单。
   - 数据处理：确定范围、清洗无效数据、按意义转化为 DataFrame（多数以一个 DF 收敛）。
   - 图表绘制：按需绘制，可多张。
   - 数据输出：展示或导出为 Markdown / HTML / 报告。

用一句话概括：**改配置而不改代码**，通过 `config.yaml` 与各区块同名文件夹，把不同的数据处理/绘图/输出类组装成一条完整流水线。
In one line: **change config, not code** — `config.yaml` plus per-sector folders assemble data-processing / charting / output classes into a full pipeline.

> 更详细的原始设计说明见 `PY_Ver/README.md`。 / See `PY_Ver/README.md` for the original detailed design.

---

# 目录 · Repository Layout

```
Lyubishchev/
├── App_Ver/     🟢 App 版（主力）/ primary
├── Web_Ver/     🟡 Web 版（自用、未完成）/ personal, WIP
└── PY_Ver/      🔴 Python 版（已归档）/ archived
```
