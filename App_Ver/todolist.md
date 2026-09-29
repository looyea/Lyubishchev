# 柳比歇夫时间统计 · 移动版（App_VER）开发准备与任务清单

> 本文件是移动原生的规划与推进清单。**当前只做安卓（Android）**，iOS 延后。

## 0. 基本定位与原则（先读这段）

1. **目标不是复用。** 移动版与 `Web_VER`、`PY_VER` 是三套独立实现。很多东西（尤其界面与交互）**复用不了，也不追求复用**。
2. **界面按手机原生重新设计。** Web 版是桌面布局（侧边栏、宽表格、多列图表），手机上体验很差。移动端要重新做**移动优先（mobile-first）**的信息架构和交互，不照搬 Web 页面。
3. **`Web_VER` 只作为「功能需求清单」参考**——即"这个软件该提供哪些能力"，而不是"代码/界面从哪搬"。
4. **数据模型可对齐但不强求一致**：分类/事件的定义（I类/II类、9 种事件）属于业务规则，可沿用；但存储层、字段命名按安卓本地最佳实践重新设计。

---

## 1. 技术选型决策（已确认）

| 项 | 决定 | 说明 |
|---|---|---|
| 路线 | **原生双端重写** | 否决 Capacitor / uni-app / Flutter / RN |
| 当前范围 | **仅 Android** | iOS 延后 |
| 语言 | Kotlin | |
| UI 框架 | Jetpack Compose（建议，待你确认） | 声明式，适合新界面 |
| 本地存储 | Room（建议） | 关系型，对标"记录 + 周结存档" |
| iOS（将来） | Swift + SwiftUI | **必须有 macOS + Xcode**，Windows 编不了 |

> ⚠️ iOS 硬约束：将来做 iOS 时，无论如何都需要一台 macOS（自购/借用/云 Mac/GitHub Actions CI 云编译）。这是苹果的强制要求。

---

## 2. 你需要准备的东西（我无法代劳）

### 2.1 必装工具链（做安卓的前置）
- [ ] **Android Studio**（最新版，自带 JDK 17、SDK Manager、模拟器、Gradle）
  - 安装时勾选下载 Android SDK 与模拟器镜像
  - 这是约 1GB 的联网安装，涉及系统环境，我不会替你执行
- [ ] 真机调试（可选但推荐）：一根数据线 + 打开手机的「USB 调试」

### 2.2 账号
- [ ] **Google Play 开发者账号**（上架用，$25 一次性）
- [ ] （将来 iOS）**Apple Developer Program**（$99/年）

### 2.3 需要你拍板的决策点
- [ ] UI 框架：**Jetpack Compose** 还是传统 **XML Views**？
- [ ] 是否认真考虑 **Kotlin Multiplatform (KMP)**：UI 各端原生、业务逻辑（数据模型/统计计算）安卓 iOS 共享，能省将来 iOS 的一部分工作量
- [ ] 应用基本信息：**应用名 / 包名（applicationId）/ 图标风格**
- [ ] 支持语言：仅中文，还是中英？
- [ ] 主题：深色为主（对齐现有 Web 暗色），还是跟随系统？

---

## 3. 功能需求盘点（来自 Web 版，作为"要做什么"，非"复用来源"）

移动端应覆盖的核心能力（界面重设计，功能对齐）：

| 模块 | 功能 | Web 对应 |
|---|---|---|
| 概览 Dashboard | 今日/本周关键指标、快速入口 | `Dashboard.vue` |
| 时间记录 TimeEntry | 录入一段活动时间（起止、分类、事件、备注） | `TimeEntry.vue` |
| 全部记录 LogManage | 记录列表、筛选（日期/分类/事件/关键词）、增删改 | `LogManage.vue` |
| 周报 WeekReport | 本周聚合 + 与上周对比 | `WeekReport.vue` |
| 历史周结 Archive | 把某周快照固化为存档、查看归档 | `SettlementArchive.vue` |
| 月报 MonthlyReport | 按月/分类/事件统计 | `MonthlyReport.vue` |
| 年报 YearReport | 年度趋势、月均、活跃天数等 | `YearReport.vue` |
| 时间分布 DistReport | 24 小时分布曲线 | `DistReport.vue` |
| 数据导入/导出 | 从 Excel/CSV 导入；备份导出 | `DataImport.vue` |

### 3.1 业务规则（沿用，属领域定义）
- 时间分类：`I类时间`、`II类时间`
- I类事件：健康、学习、阅读、产出、投资、社交
- II类事件：思考、整理、兴趣
- 记录字段：日期、开始、结束、历时分钟、分类、事件、备注、ISO 周数、年
- 周结：把某周聚合快照固化存档

### 3.2 图表类型（原生库重画，样式不复用 Web）
饼图/环形、雷达图、柱状、折线、24 小时分布面积图。安卓图表库候选：**Vico**（Compose 原生）/ **MPAndroidChart**（Views）。雷达图支持需重点确认。

---

## 4. 移动端界面重规划（≠ Web 布局）

建议的移动信息架构（待细化）：
- **底部导航（Bottom Bar）**：概览 / 记录 / 报表 / 我的（4 段），替代 Web 侧边栏
- **时间记录**：快捷录入优先——大按钮、时间滚轮选择器、事件芯片（chips），单手操作
- **报表**：Web 的多列并排 → 移动改为**上下堆叠 + 卡片**，图表可横向滑动
- **全部记录**：用列表 + 下拉筛选 + 长按操作，替代宽表格
- **导入/备份**：手机场景改为文件选取 / 分享导入，而非拖拽
- 触控目标 ≥ 48dp，适配刘海/状态栏安全区、深色主题

---

## 5. 建议技术栈（安卓）

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 架构：MVVM（ViewModel + 协程 + Flow）
- 数据：Room（本地库）、DataStore（偏好设置）
- 导航：Navigation Compose
- 图表：Vico 或 MPAndroidChart
- 依赖注入：Hilt（可选）
- 构建：Gradle (Kotlin DSL) + version catalog

---

## 6. 目录规划

```
App_VER/
├── todolist.md          ← 本文件
├── android/             ← 现在做：Android Studio / Gradle 工程
└── ios/                 ← 以后做：Xcode 工程（需 Mac）
```
（若走 KMP，则改为共享 `shared/` + `androidApp/` + `iosApp/`，届时重规划）

---

## 7. 分阶段推进计划

### Phase 0 · 环境（你完成）
- [ ] 装 Android Studio + SDK；能新建并运行一个空模板工程到模拟器/真机

### Phase 1 · 工程骨架（我来，需工具链就绪后验证）
- [ ] 在 `App_VER/android/` 初始化 Gradle + Compose 工程
- [ ] 目录分层、依赖、主题、底部导航框架
- [ ] 一个空壳可编译通过

### Phase 2 · 数据层
- [ ] Room 实体/DAO/Database（记录、周结存档）
- [ ] Repository + Flow
- [ ] 统计聚合逻辑（周/月/年/分布）

### Phase 3 · 核心闭环：记录
- [ ] 时间记录录入界面（快捷、移动优先）
- [ ] 全部记录列表 + 筛选 + 增删改

### Phase 4 · 报表与图表
- [ ] Dashboard、周报/周结、月报、年报、时间分布
- [ ] 各图表原生实现

### Phase 5 · 数据与打磨
- [ ] 导入/导出/备份
- [ ] 深色/安全区/手势/空态
- [ ] debug APK 打包装机的真机联调

### Phase 6 · 发布（你主导）
- [ ] 签名 keystore、`bundleRelease`、Play 商店素材与上架

### 将来 · iOS
- [ ] 评估复用 Phase 2 逻辑（取决于是否走 KMP）
- [ ] Mac/SwiftUI/Xcode 打包与上架

---

## 8. 我能做 / 不能做

**✅ 我能做**：生成工程骨架、Gradle/依赖配置、Kotlin+Compose 代码、Room 数据层与统计逻辑、各功能界面实现、图表接入、签名与构建配置、写开发文档。

**❌ 我不能做**：安装 Android Studio/SDK、注册 Google/Apple 账号、商店上架与审核、在没有 Mac 时编译 iOS。

---

## 9. 风险与注意

- **工作量**：原生重写 + 不复用，界面/图表/逻辑全要重做，周期长，建议按 Phase 里程碑推进。
- **图表**：ECharts 在原生无对应库，复杂图表（雷达、24h 分布）需验证目标库支持度。
- **iOS 编译依赖 Mac**：早规划（自建/云/CI）。
- **数据迁移**：若要把 Web 版已录数据搬到手机，需单独设计导出/导入通道（Web 导出 → 安卓导入），非自动。

---

## 10. 下一步（阻塞项）

1. **你先装好 Android Studio**，并跑通一个空模板工程（Phase 0）。
2. 回复第 2.3 节的决策点（尤其 **UI 框架** 和 **是否走 KMP**）。

两项就绪后，我就在 `App_VER/android/` 初始化可编译的 Compose 工程骨架（Phase 1）。
