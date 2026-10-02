package com.lyubishchev.timekeeper.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.theme.NumberSerif

/** 当前版本号：与 build.gradle.kts 的 versionName 保持一致 */
object AppVersions {
    const val CURRENT = "0.92"

    data class Release(val version: String, val notes: List<String>)

    /** 最新版本在前，最早版本在后；同周期小修用 0.62、0.63 这类细分 */
    val history = listOf(
        Release(
            "0.92",
            listOf(
                "新增多语种：中文 / English / Español / Português / Deutsch / Nederlands / 日本語，设置 →「语言」随时切换，选「跟随系统」时按手机系统语言自动适配。",
                "新增启动 Splash 页：居中显示与桌面一致的应用图标，应用名按语言显示（中文「柳比歇夫时间管理」/ 英文「Lyubishchev Time」），随后进入主界面。",
                "自己定义的分类、事件与备注属于个人数据，一律不翻译——原来填的什么语言就显示什么语言。",
                "导出报告的正文、表头与星期、时长单位也会跟随应用语言；CSV 仍能被全量导入识别。",
                "「主题」再加六套控件配色：朱砂红 / 琥珀黄 / 蔷薇粉 / 石墨灰 / 曜石黑 / Solarized（终端配色老熟人），浅色深色各自适配，底色仍是莫兰迪。",
                "设置新增「报告模板」页：默认 Markdown 模板，可切纯文本，也可上传自己的 .md/.txt（本版只登记选用，解析留后续）；另加「AI API」入口，暂只占位。",
                "启动屏补了三项，文字全部居中：应用名下方显示「已陪伴您 N 天」（从第一次运行那天起算），其下为斜体 *LOOYEA* 出品署名，屏幕最底部一行小字「纯本地时间统计，数据只存手机」。",
                "设置页顶部统计卡改为三行：已陪伴您 N 天 / 累计记录 X 小时 Y 分 / 已记录 N 条；卡片副标题换成「与时间相伴，就是与生命相随。记住时间就是记住生命。」。",
                "各语言的「已陪伴您 N 天」「已记录 N 条」这类计数句改用单复数规则，1 天、1 条不再写成复数。",
                "「I类时间 / II类时间」属于系统固定文字，现在随界面语言显示对应译名（数据库与统计仍按原名匹配）；设置里的每一行统一为名称左对齐、进入次级页的箭头贴右。",
            ),
        ),
        Release(
            "0.91",
            listOf(
                "「主题」正式上线：四套控件配色一键整组切换（莫兰迪灰绿 / 蒂芙尼蓝 / 海运蓝 / 森林绿），浅色与深色各自适配，中性底色仍保持莫兰迪极简。",
                "记录页年模式的日期按钮改为置灰而非隐藏，位置稳定。",
                "分段按钮（概览切换、日/周/月/年）去掉选中态的对勾，只以底色区分。",
                "设置页精简：时间分类、按需导出两行去掉右侧说明文字，只留进入次级页的箭头。",
            ),
        ),
        Release(
            "0.9",
            listOf(
                "概览页补上「本月累计记录」：与今日、本周并排三列，正好对上雷达图下方今日/本周/本月三个时间档。",
                "记录页周视图不再只显示三周：按 GROUP BY 周把今年有记录的周全部列出（一条记录都没有的周自然不占位置），倒序、默认全部收起。",
                "记录页月视图同理：今年有记录的月份各占一节，不再固定只给最近三个月。",
                "日期选择器不再能选未来：周、月两种模式下限到今年年初、上限到今天，下周和下月都点不了。日视图默认停在今天，看以前哪天就点日期选。",
                "记录页年视图改为只列出真正有记录的年份（例如大前年、去年、今年，中间空白的年份不出现），每年一块直接铺开一类/二类与逐事件合计，此时不需要日期锚点。",
                "设置页「常用」改名「快捷设置」，右侧说明文字去掉只留箭头，进入后的标题同步。",
            ),
        ),
        Release(
            "0.8",
            listOf(
                "界面换装「莫兰迪极简」：暖米白 / 炭灰底色，主色改为灰绿，辅色陶土，深浅两套配色都重新调过，对比度按可读性校过。",
                "排版层级重做：概览的今日/本周合计改成大号衬线数字，一眼看到花了多少时间；分段选择器换成低饱和的浅色选中态，不再是一片纯色块。",
                "字号阶梯补齐（正文、小标签、按钮文字都给了字距），卡片描边、分隔线改用更柔的轮廓色。",
            ),
        ),
        Release(
            "0.7",
            listOf(
                "全量导出升级：可直接导出 SQLite 数据库文件（.db，单文件携带全部记录，导出前已合并日志），CSV 表格仍是可选项。",
                "新增「全量导入」：选择 .db 数据库或本 App 导出的 CSV，逐条与主数据库比对后合并入库；同日期、同起止、同事件、同时长的记录视为重复自动跳过，不会双计。",
                "导入完成会报告新增/跳过/解析条数；文件无法识别（非本 App 库、缺表、格式不符）会给出明确错误。",
                "版本号规则细化：功能上新进 0.x，同一功能周期内的小修复用 0.x1、0.x2 这类补丁号。",
            ),
        ),
        Release(
            "0.62",
            listOf(
                "修复雷达图消失：所选期间内有记录的事件不足 3 个时图无法成形，现用设置里的事件补足到 3 根轴（补进来的显示 0）。",
            ),
        ),
        Release(
            "0.6",
            listOf(
                "雷达图轴改为跟随数据库：所选期间内有记录的事件各占一轴——事件即使被删除或改名，只要该期间留有记录就照样显示；期间内没记录的事件不占轴。",
                "雷达时间档新增「本月」：本月对比上月，逻辑与今日/本周完全一致，只是范围换成整月。",
            ),
        ),
        Release(
            "0.5",
            listOf(
                "「我的」改名为「设置」，原设置占位行换成「版本」，进入本页查看更新历史。",
                "新增「全量导出」：一键把记录数据库里的全部记录导出为 CSV（Excel 可直接打开），再经分享面板保存或发送。",
                "原「导出」更名「按需导出」，功能不变：选区间、选 MD/TXT/Excel/HTML 四种格式。",
                "分类模型简化为固定两类、事件两层：分类不再增删改，取消三级子类；事件可新建、改名、删除，每类最多 8 个。",
                "概览雷达卡片去掉与上方统计重复的大数字，图例居中、只显示颜色与今天/昨天标签。",
            ),
        ),
        Release(
            "0.4",
            listOf(
                "导出报告上线：按日期区间导出，含总计、分类与逐事件汇总、雷达图与逐条明细（年月日/星期/类别/事件/起止/时长/备注）。",
                "「主题」更名为「颜色模式」；新增主题风格入口（小清新、都市、沉稳商务，规划中）。",
                "修复过午夜后概览、记录、记一笔、导出仍停在旧一天的问题。",
            ),
        ),
        Release(
            "0.3",
            listOf(
                "记一笔重排：日期行（仅补充记录可改）+ 补充记录勾选，开始/结束各用独立时间选择器，时长自动计算。",
                "「常用」上线：配置分类+事件+时长的快捷组合，记一笔里一键套用。",
                "「时间分类」维护页：事件的新建、改名、删除。",
            ),
        ),
        Release(
            "0.2",
            listOf(
                "概览页雷达图卡片：一类/二类切换，今日对比昨天、本周对比上周。",
                "记录页四档浏览：日/周/月/年，日期锚点选择 + 手风琴式汇总。",
                "颜色模式：跟随系统/浅色/深色，整体配色美化。",
            ),
        ),
        Release(
            "0.1",
            listOf(
                "首个真机可用版本：概览、记录、记一笔、报表占位、我的五页框架。",
                "本地 Room 数据库存储，数据只存手机，无需联网与账号。",
            ),
        ),
    )
}

/**
 * 版本历史：按版本号倒序列出每次改动说明，最新在上。
 * Version history: release notes, newest first.
 */
@Composable
fun VersionHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "‹ " + stringResource(R.string.cat_back),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(top = 6.dp, end = 12.dp, bottom = 6.dp),
            )
            Text(
                text = stringResource(R.string.version_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.version_hint, AppVersions.CURRENT),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))
        releases().forEachIndexed { index, release ->
            VersionCard(release = release, isCurrent = index == 0)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 每个版本的说明都有七语版本：中文直接用下面 `history` 里的原文（当初逐轮写下的存档），
 * 其余语种读各自的 `release_notes_*`。新增版本时两边都要补，否则非中文语种会少一版。
 */
private val localizedNotes = listOf(
    "0.92" to R.string.release_notes_092,
    "0.91" to R.string.release_notes_091,
    "0.9" to R.string.release_notes_09,
    "0.8" to R.string.release_notes_08,
    "0.7" to R.string.release_notes_07,
    "0.62" to R.string.release_notes_062,
    "0.6" to R.string.release_notes_06,
    "0.5" to R.string.release_notes_05,
    "0.4" to R.string.release_notes_04,
    "0.3" to R.string.release_notes_03,
    "0.2" to R.string.release_notes_02,
    "0.1" to R.string.release_notes_01,
)

@Composable
private fun releases(): List<AppVersions.Release> =
    if (AppLocale.isChinese) {
        AppVersions.history
    } else {
        localizedNotes.map { (version, notesRes) ->
            AppVersions.Release(version, stringResource(notesRes).lines())
        }
    }

@Composable
private fun VersionCard(release: AppVersions.Release, isCurrent: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "v${release.version}",
                style = NumberSerif,
                color = MaterialTheme.colorScheme.primary,
            )
            if (isCurrent) {
                Text(
                    text = stringResource(R.string.version_current_tag),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        release.notes.forEach { note ->
            Text(
                text = "· $note",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
