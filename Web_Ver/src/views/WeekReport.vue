<template>
  <div>
    <div class="page-header">
      <h2>周报</h2>
      <p>以周为单位的时间使用分析，本周数据为主，穿插上周对比</p>
    </div>

    <!-- 周导航 -->
    <div class="chart-container">
      <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
        <el-button-group>
          <el-button @click="goPrevWeek">上一周</el-button>
          <el-button @click="goThisWeek">本周</el-button>
          <el-button @click="goNextWeek">下一周</el-button>
        </el-button-group>
        <el-date-picker
          v-model="anchorDate"
          type="date"
          placeholder="跳转到指定周"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 170px"
        />
        <el-tag type="primary" size="large">
          {{ weekRange.year }} 年第 {{ weekRange.weekNumber }} 周
        </el-tag>
        <span style="color: #909399; font-size: 13px">
          {{ weekRange.start }} ~ {{ weekRange.end }}
          （对比期：{{ prevWeekRange.start }} ~ {{ prevWeekRange.end }}）
        </span>
        <div style="flex: 1" />
        <ExportButton target-id="week-report" file-name="周报" />
      </div>
    </div>

    <template v-if="hasAnyRecord">
      <!-- 本周概览 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <StatCard title="本周总计" :value="current.totalMinutes" />
        </el-col>
        <el-col :span="6">
          <StatCard title="日均用时" :value="dailyAverage" />
        </el-col>
        <el-col :span="6">
          <StatCard title="记录天数" :value="activeDayCount" format="number" unit="/ 7 天" />
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-card__title">总量环比</div>
            <div>
              <span class="stat-card__value" :class="rateClass(totalChangeRate)">
                {{ totalChangeRate > 0 ? '+' : '' }}{{ totalChangeRate }}%
              </span>
            </div>
            <div style="font-size: 12px; color: #909399; margin-top: 4px">
              上周 {{ formatMinutes(previous.totalMinutes) }}
            </div>
          </div>
        </el-col>
      </el-row>

      <div id="week-report">
        <!-- 逐日节奏对比 -->
        <div class="chart-container">
          <div class="chart-container__title">每日用时节奏（本周 vs 上周）</div>
          <DailyCompareChart
            :labels="dayLabels"
            :current="dailySeries.current"
            :previous="dailySeries.previous"
          />
        </div>

        <el-row :gutter="20">
          <!-- 分类对比 -->
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-container__title">时间分类：本周与上周</div>
              <CompareChart
                :items="ALL_CATEGORIES"
                :current="categorySeries.current"
                :previous="categorySeries.previous"
              />
            </div>
          </el-col>
          <!-- 本周分类构成 -->
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-container__title">本周时间构成</div>
              <CategoryPieChart
                :data="toPieData(categorySeries.current, ALL_CATEGORIES)"
                title=""
              />
            </div>
          </el-col>
        </el-row>

        <!-- I类事件雷达对比 -->
        <div class="chart-container">
          <div class="chart-container__title">I类时间事件分布（六边形叠加对比）</div>
          <CompareChart
            :items="L1_EVENTS"
            :current="l1Series.current"
            :previous="l1Series.previous"
            :show-bar="false"
            :show-radar="true"
          />
        </div>

        <!-- II类事件雷达对比 -->
        <div class="chart-container">
          <div class="chart-container__title">II类时间事件分布（三角叠加对比）</div>
          <CompareChart
            :items="L2_EVENTS"
            :current="l2Series.current"
            :previous="l2Series.previous"
            :show-bar="false"
            :show-radar="true"
          />
        </div>

        <!-- 事件逐项对比柱状 -->
        <div class="chart-container">
          <div class="chart-container__title">各事件耗时环比</div>
          <CompareChart
            :items="ALL_EVENTS"
            :current="eventSeries.current"
            :previous="eventSeries.previous"
          />
        </div>

        <!-- 明细表 -->
        <div class="chart-container">
          <div class="chart-container__title">周报明细</div>
          <el-table :data="eventTable" stripe border size="small" show-summary :summary-method="summaryMethod">
            <el-table-column prop="event" label="事件" width="100" />
            <el-table-column prop="category" label="分类" width="110" />
            <el-table-column prop="current" label="本周(分钟)" width="120" sortable />
            <el-table-column prop="previous" label="上周(分钟)" width="120" sortable />
            <el-table-column label="增减" width="110" sortable :sort-by="'diff'">
              <template #default="{ row }">
                <span :class="rateClass(row.diff)">
                  {{ row.diff > 0 ? '+' : '' }}{{ row.diff }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="环比" width="110">
              <template #default="{ row }">
                <el-tag
                  v-if="row.rate !== 0"
                  :type="row.rate > 0 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.rate > 0 ? '+' : '' }}{{ row.rate }}%
                </el-tag>
                <span v-else class="rate-flat">—</span>
              </template>
            </el-table-column>
            <el-table-column label="本周占比" min-width="160">
              <template #default="{ row }">
                <el-progress
                  :percentage="current.totalMinutes ? Math.round(row.current / current.totalMinutes * 100) : 0"
                  :stroke-width="10"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 周结区 -->
      <div class="chart-container">
        <div class="chart-container__title">周结</div>
        <div v-if="settlement" style="margin-bottom: 16px">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              本周已于 {{ dayjs(settlement.settledAt).format('YYYY-MM-DD HH:mm') }} 结存
            </template>
            <template #default>
              结存快照：总计 {{ formatMinutes(settlement.summary.totalMinutes) }}
              <span v-if="settlement.note"> / 备注：{{ settlement.note }}</span>
            </template>
          </el-alert>
        </div>
        <div v-else style="margin-bottom: 16px; color: #909399; font-size: 13px">
          本周尚未结存。结存会将当前统计结果固化为快照归档，之后补录记录不会影响已结存的快照。
        </div>
        <el-form :inline="true" size="default">
          <el-form-item label="周备注">
            <el-input v-model="settleNote" placeholder="本周小结、下周计划（可选）" style="width: 320px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="settling" @click="handleSettle">
              {{ settlement ? '重新结存' : '结存本周' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </template>

    <el-empty v-else description="本周暂无记录数据">
      <el-button type="primary" @click="$router.push('/entry')">去记录本周时间</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useTimeLogStore } from '@/stores/timeLog'
import { useWeekReport } from '@/composables/useWeekReport'
import { formatMinutes } from '@/utils/timeUtils'
import { ALL_CATEGORIES, ALL_EVENTS, L1_EVENTS, L2_EVENTS } from '@/types'
import StatCard from '@/components/common/StatCard.vue'
import ExportButton from '@/components/common/ExportButton.vue'
import CompareChart from '@/components/charts/CompareChart.vue'
import DailyCompareChart from '@/components/charts/DailyCompareChart.vue'
import CategoryPieChart from '@/components/charts/CategoryPieChart.vue'

const store = useTimeLogStore()
const {
  anchorDate,
  weekRange,
  prevWeekRange,
  dayLabels,
  current,
  previous,
  hasAnyRecord,
  categorySeries,
  l1Series,
  l2Series,
  eventSeries,
  dailySeries,
  activeDayCount,
  dailyAverage,
  totalChangeRate,
  eventTable,
  settlement,
  goPrevWeek,
  goNextWeek,
  goThisWeek,
  settleWeek,
  loadSettlements,
} = useWeekReport()

const settling = ref(false)
const settleNote = ref('')

function rateClass(v: number) {
  if (v > 0) return 'rate-up'
  if (v < 0) return 'rate-down'
  return 'rate-flat'
}

function toPieData(values: number[], names: string[]) {
  return names
    .map((name, i) => ({ name, value: values[i] || 0 }))
    .filter(d => d.value > 0)
}

function summaryMethod({ columns, data }: any) {
  const sums: string[] = []
  columns.forEach((col: string, idx: number) => {
    if (idx === 0) {
      sums[idx] = '合计'
      return
    }
    if (col === '本周(分钟)' || col === '上周(分钟)') {
      const key = col === '本周(分钟)' ? 'current' : 'previous'
      sums[idx] = String(data.reduce((s: number, r: any) => s + (r[key] || 0), 0))
    } else {
      sums[idx] = ''
    }
  })
  return sums
}

async function handleSettle() {
  settling.value = true
  try {
    await settleWeek(settleNote.value || undefined)
    ElMessage.success('本周已结存')
  } catch (e: any) {
    ElMessage.error(`结存失败: ${e.message}`)
  } finally {
    settling.value = false
  }
}

onMounted(async () => {
  await store.loadAll()
  await loadSettlements()
})
</script>
