# 柳比歇夫时间日志 · Lyubishchev Time-Log

> 一个把「柳比歇夫时间记录法」落地的个人项目，同一套理念先后有三种实现：**移动端 App**、**Python 数据处理**、**Web 网站**。
> A personal project implementing the *Lyubishchev time-tracking method*, built three times over: a **mobile App**, a **Python data-processing** tool, and a **Web** site.

## 三种版本一览 · Three Versions at a Glance

| 版本 / Version | 状态 / Status | 说明 / Note |
| --- | --- | --- |
| **App 版**（`App_Ver/`） | 🟢 **主力 · 当前在用** / **Primary, actively developed** | 原生 Android、纯离线、随身记录，界面支持七种语言。v0.92。Native Android, fully offline, record on the go, with a seven-language UI. v0.92. |
| **Web 版**（`Web_Ver/`） | 🟡 **自用 · 尚未开发完** / **Personal use, work-in-progress** | Vue 3 网站，仅供个人分析使用，功能仍在补齐。Vue 3 site for personal analysis, not feature-complete. |
| **Python 版**（`PY_Ver/`） | 🔴 **已归档 · 不再维护** / **Archived, no longer maintained** | 2021 年归档的配置驱动报告工具，仅作历史参考。Config-driven report tool, archived in 2021, kept for reference only. |

> 一句话：**日常记录用 App，深度分析偶尔用 Web，Python 已经退役。**
> In short: **log daily with the App, occasionally analyze on the Web, Python has retired.**

---

# 一、App 版（主力） · The App (Primary)

原生安卓应用，采用 **Jetpack Compose + Material 3 + Room**，**纯离线**——数据只存在手机本地，无需联网、无需账号。当前版本 **0.92**。
A native Android app built with **Jetpack Compose + Material 3 + Room**, **fully offline** — data lives only on the phone, no network or account required. Current version **0.92**.

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
├── i18n/       AppLocale.kt    七语目录与 Locale 切换 / seven-language locale switching
└── ui/         界面层 · UI layer
    ├── AppRoot / AppBottomBar / AppTab / SplashGate   底部五入口外壳、切换与启动屏 / shell, 5-slot navigation, splash
    ├── screen/  各页面 / screens：Home · Log · AddRecord · Mine · CategoryEditor · QuickPickEditor · ReportTemplate · VersionHistory …
    ├── export/  按需导出 / 全量导入界面与 ViewModel
    ├── widget/  RadarChart 雷达图、ChoiceRow 等复用组件 / reusable widgets
    └── theme/   颜色模式、主题配色、字体排印 / color mode, theme, typography
```

技术栈：Kotlin、Compose (Material 3)、Room + KSP、Java 17、`minSdk 26`。
Stack: Kotlin, Compose (Material 3), Room + KSP, Java 17, `minSdk 26`.

## 1.2 逻辑结构（这玩意怎么用） · Logic Structure (how it works & how you use it)

底部是**五个一级入口**（中间是抖音式的「记一笔」快捷按钮）：
Five top-level destinations in the bottom bar (the center slot is a TikTok-style "log now" button):

| 入口 | Tab | 用途 / Purpose |
| --- | --- | --- |
| 概览 | **Home** | 今日 / 本周 / 本月累计，一张雷达图做「今天 vs 昨天」「本周 vs 上周」「本月 vs 上月」的对比 / today, this-week, this-month totals with a radar comparing day/week/month vs the previous one |
| 记录 | **Log** | 按 **日 / 周 / 月 / 年** 四档浏览历史记录，手风琴式展开每条明细 / browse history by day / week / month / year with accordion detail |
| 记一笔 | **Add** | 登记一条时间；开始时间自动承接上一条最新结束时间，时长按起止自动算；可用「快捷设置」一键套用常用组合 / log an entry; start time auto-follows the latest end; duration auto-computed; quick-pick presets for one-tap entry |
| 报表 | **Report** | ⚠️ 规划中的占位页 / placeholder — planned, not yet implemented |
| 设置 | **Mine** | 时间分类、快捷设置、按需导出、全量导入/导出、报告模板、AI API（占位）、颜色模式、主题配色、语言、版本历史 / categories, quick-picks, on-demand export, full import/export, report templates, AI API (placeholder), color mode, themes, language, version history |

核心数据模型是一条 `time_logs` 记录：**日期 + 开始/结束时间 + 时长 + 类别 + 事件 + 备注**。类别固定为 **I 类时间**（维持生存与成长）与 **II 类时间**（为时间本身服务的时间），事件是可新建、改名、删除的两层结构（每类最多 8 个）。`date`、时间以文本存储，`week_number` / `year` 在写入时算好，让周、年统计走索引。
The core model is one `time_logs` row: **date + start/end time + duration + category + event + note**. Categories are fixed as **Class-I** (sustenance & growth) and **Class-II** (upkeep of the system); events are a two-level list you can add/rename/delete (max 8 per category). Dates/times are stored as text while `week_number` / `year` are denormalized at write time so week/year stats hit indexes.

导出与同步：**按需导出**按日期区间生成含总计、分类逐事件汇总、雷达图与逐条明细的报告，支持 MD / TXT / Excel / HTML；**全量导出**可一键导出 SQLite 数据库文件（.db）或 CSV；**全量导入**读取本 App 导出的 .db / CSV 逐条比对合并，同日期、同起止、同事件、同时长视为重复自动跳过、不双计。
Export & sync: **on-demand export** produces a report for a date range (totals, per-category/event summary, radar, line-by-line detail) in MD / TXT / Excel / HTML; **full export** dumps the whole DB as a SQLite `.db` file or CSV; **full import** reads a `.db` / CSV exported by this app and merges row by row, skipping duplicates (same date, span, event, duration) so nothing is double-counted.

外观可调配：**颜色模式**支持跟随系统 / 浅色 / 深色；**主题**提供十套控件配色一键整组切换（莫兰迪灰绿 / 蒂芙尼蓝 / 海运蓝 / 森林绿 / 朱砂红 / 琥珀黄 / 蔷薇粉 / 石墨灰 / 曜石黑 / Solarized），浅色与深色各自适配，中性底色始终是莫兰迪。
Appearance: **color mode** follows system / light / dark; **themes** offer ten control-palette sets switchable in one tap (Morandi grey-green / Tiffany blue / navy / forest green / crimson / amber / rose / graphite / obsidian / Solarized), each tuned for light and dark, on a constant Morandi neutral base.

多语种与启动屏：界面支持**中文 / English / Español / Português / Deutsch / Nederlands / 日本語**七种语言，可在「设置 → 语言」随时切换，选「跟随系统」时按手机系统语言自动适配；应用内外的文本（导出报告的正文、表头、星期、时长单位、固定的两个一级分类「I类时间 / II类时间」，乃至整份版本历史）都随语言走，计数句（陪伴天数、记录条数）还按各语言的单复数规则变化，**但你自己填写的事件、快捷条目与备注属于个人数据，一律不翻译**。启动时先经过 Splash 页：居中图标、按语言显示的应用名、「已陪伴您 N 天」（自首次运行起算）与 *LOOYEA* 出品署名。
Languages & splash: the UI ships in **seven languages — 中文 / English / Español / Português / Deutsch / Nederlands / 日本語**, switchable under Settings → Language, with a "follow the system" option; interface text, exported reports, the two fixed top-level classes (Class I / Class II time) and the whole in-app changelog follow the language, and count lines such as days together and entries logged follow each language's singular/plural rules, **while events, quick picks and notes you wrote are personal data and are never translated**. Launching shows a splash screen: centered icon, localized app name, "days together since first launch" and the italic *LOOYEA* credit.

报告模板与 AI API：「设置 → 报告模板」可选用内置 Markdown 或纯文本模板，也能上传自己的 `.md` / `.txt`（文本内容需按 Markdown 书写）；本版只做登记与选用，模板解析留待后续。「AI API」目前只是入口占位，用于将来让报告自选推理引擎与调用方式，不进行任何联网配置。
Report templates & AI API: Settings → Report templates lets you pick the built-in Markdown or plain-text template, or upload your own `.md` / `.txt` (whose content must still be written in Markdown); this release only registers and selects them — parsing comes later. The **AI API** row is a placeholder entry for a future choice of reasoning engine / API method, with no network configuration at all.

## 1.3 版本进化 · Version History

App 内「设置 → 版本」记录了完整更新历史（当前 **v0.92**，且这份历史本身也翻了七种语言）。几个里程碑：
The in-app **Settings → Version** screen keeps the full changelog (currently **v0.92**, itself translated into all seven languages). Key milestones:

- **v0.1** 首个真机可用版：概览、记录、记一笔、报表占位、我的五页框架，本地 Room 存储 / first on-device build: the five-page shell, local Room storage.
- **v0.2–0.3** 雷达图对比、日/周/月/年四档浏览、「记一笔」重排、「常用（快捷设置）」上线 / radar comparisons, four-range browsing, reworked log form, quick-pick presets.
- **v0.4–0.5** 导出报告、全量导出 CSV、分类模型简化为固定两类、事件两层 / export reports, full CSV export, categories simplified to fixed two classes with two-level events.
- **v0.6–0.7** 雷达轴跟随数据库、新增「本月」档、全量导出 .db、新增全量导入 / radar axes follow the DB, "this month" range, `.db` full export, full import.
- **v0.8–0.9** 「莫兰迪极简」换装、大号衬线数字、记录页按有记录的周/月/年动态列出 / "Morandi minimal" redesign, large serif numbers, dynamic week/month/year listing.
- **v0.91** 主题正式上线（四套配色一键切换）及若干界面精简 / themes shipped (four switchable palettes) plus UI trimming.
- **v0.92** 七语种界面 + 启动 Splash、主题扩到十套、报告模板页与 AI API 入口、设置页统计卡三行（陪伴天数 / 累计时长 / 记录条数）/ seven-language UI plus splash, themes grown to ten palettes, report-template screen and AI API entry, three-line stats card in Settings.

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
