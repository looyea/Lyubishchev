<template>
  <div>
    <div class="page-header">
      <h2>年报</h2>
      <p>全年时间使用的回顾与总结</p>
    </div>

    <!-- 年份选择 -->
    <div class="chart-container">
      <el-form :inline="true" size="default">
        <el-form-item label="年份">
          <el-select v-model="selectedYear" style="width: 140px">
            <el-option v-for="y in availableYears" :key="y" :label="`${y} 年`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <ExportButton target-id="year-report" file-name="年度报告" />
        </el-form-item>
      </el-form>
    </div>

    <template v-if="yearData.totalMinutes > 0">
      <!-- 年度 KPI -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <StatCard title="全年总计" :value="yearData.totalMinutes" />
        </el-col>
        <el-col :span="6">
          <StatCard title="记录天数" :value="yearData.activeDays" format="number" unit="天" />
        </el-col>
        <el-col :span="6">
          <StatCard title="日均用时" :value="dailyAvg" />
        </el-col>
        <el-col :span="6">
          <StatCard title="记录条数" :value="yearData.recordCount" format="number" unit="条" />
        </el-col>
      </el-row>

      <div id="year-report">
        <!-- 月度分类堆叠柱状 -->
        <div class="chart-container">
          <div class="chart-container__title">月度时间分类构成</div>
          <v-chart :option="monthlyStackOption" autoresize style="height: 380px" />
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-container__title">月均每日用时趋势</div>
              <v-chart :option="dailyAvgOption" autoresize style="height: 340px" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-container__title">全年时间分类占比</div>
              <CategoryPieChart :data="categoryPie" title="" />
            </div>
          </el-col>
        </el-row>

        <!-- 年度事件雷达 -->
        <div class="chart-container">
          <div class="chart-container__title">全年事件分布雷达</div>
          <v-chart :option="yearRadarOption" autoresize style="height: 420px" />
        </div>

        <!-- 事件排行 -->
        <div class="chart-container">
          <div class="chart-container__title">事件耗时排行</div>
          <el-table :data="ranking" stripe border size="small">
            <el-table-column type="index" label="#" width="55" />
            <el-table-column prop="event" label="事件" width="100" />
            <el-table-column prop="category" label="分类" width="110" />
            <el-table-column label="全年用时" width="130">
              <template #default="{ row }">{{ formatMinutes(row.minutes) }}</template>
            </el-table-column>
            <el-table-column label="占比" min-width="200">
              <template #default="{ row }">
                <el-progress :percentage="row.percent" :stroke-width="12" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>

    <el-empty v-else description="该年暂无记录数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { useTimeLogStore } from '@/stores/timeLog'
import { useReportGenerator } from '@/composables/useReportGenerator'
import { useChartTheme } from '@/composables/useChartTheme'
import { aggregateByYear } from '@/utils/dataAggregator'
import { formatMinutes, yearMonthList } from '@/utils/timeUtils'
import { ALL_CATEGORIES, ALL_EVENTS, L1_EVENTS } from '@/types'
import StatCard from '@/components/common/StatCard.vue'
import CategoryPieChart from '@/components/charts/CategoryPieChart.vue'
import ExportButton from '@/components/common/ExportButton.vue'

const store = useTimeLogStore()
const { availableYears } = useReportGenerator()
const { ACCENT_COLORS, getRadarOption } = useChartTheme()

const selectedYear = ref(new Date().getFullYear())

const yearData = computed(() => aggregateByYear(store.records, selectedYear.value))

const months = computed(() => yearMonthList(selectedYear.value))
const monthLabels = computed(() => months.value.map(m => `${parseInt(m.slice(5))}月`))

const dailyAvg = computed(() => {
  const sum = Object.values(yearData.value.monthDailyAvg).reduce((s, v) => s + v, 0)
  const active = Object.values(yearData.value.monthDailyAvg).filter(v => v > 0).length
  return active === 0 ? 0 : Math.round(sum / active)
})

/** 月度分类堆叠柱状图 */
const monthlyStackOption = computed(() => {
  const catTotals = yearData.value.monthCategoryTotals
  return {
    color: [ACCENT_COLORS[0], ACCENT_COLORS[1]],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, data: [...ALL_CATEGORIES] },
    grid: { left: '3%', right: '4%', top: 24, bottom: '14%', containLabel: true },
    xAxis: { type: 'category', data: monthLabels.value },
    yAxis: { type: 'value', name: '分钟' },
    series: ALL_CATEGORIES.map((cat, i) => ({
      name: cat,
      type: 'bar' as const,
      stack: 'total',
      data: months.value.map(m => catTotals[m]?.[cat] || 0),
      itemStyle: { color: ACCENT_COLORS[i] },
    })),
  }
})

/** 月均每日用时折线 */
const dailyAvgOption = computed(() => ({
  color: [ACCENT_COLORS[4]],
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const p = params[0]
      const v = p.value as number
      return `${p.axisValue}<br/>${p.marker}日均: ${Math.floor(v / 60)}小时${v % 60}分`
    },
  },
  grid: { left: '3%', right: '4%', top: 24, bottom: '12%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: monthLabels.value },
  yAxis: { type: 'value', name: '分钟' },
  series: [{
    name: '日均用时',
    type: 'line',
    smooth: true,
    areaStyle: { opacity: 0.2 },
    data: months.value.map(m => yearData.value.monthDailyAvg[m] || 0),
  }],
}))

const categoryPie = computed(() =>
  ALL_CATEGORIES
    .map(cat => ({ name: cat, value: yearData.value.categoryTotals[cat] || 0 }))
    .filter(d => d.value > 0),
)

/** 全年事件雷达 */
const yearRadarOption = computed(() => {
  const indicators = ALL_EVENTS.map(evt => ({
    name: evt,
    max: Math.ceil((yearData.value.eventTotals[evt] || 0) * 1.2) || 600,
  }))
  return getRadarOption(
    '',
    indicators,
    [{ name: `${selectedYear.value} 年`, values: ALL_EVENTS.map(e => yearData.value.eventTotals[e] || 0) }],
  )
})

const ranking = computed(() => {
  const total = yearData.value.totalMinutes || 1
  return ALL_EVENTS
    .map(evt => ({
      event: evt,
      category: L1_EVENTS.includes(evt as any) ? 'I类时间' : 'II类时间',
      minutes: yearData.value.eventTotals[evt] || 0,
      percent: Math.round((yearData.value.eventTotals[evt] || 0) / total * 100),
    }))
    .sort((a, b) => b.minutes - a.minutes)
})

onMounted(async () => {
  await store.loadAll()
  if (availableYears.value.length > 0) {
    selectedYear.value = availableYears.value[availableYears.value.length - 1]
  }
})
</script>
