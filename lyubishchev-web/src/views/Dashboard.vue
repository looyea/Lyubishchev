<template>
  <div>
    <div class="page-header">
      <h2>概览</h2>
      <p>{{ weekRange.start }} ~ {{ weekRange.end }}（{{ weekRange.year }} 年第 {{ weekRange.weekNumber }} 周）</p>
    </div>

    <!-- 本周关键指标 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <StatCard title="今日已记录" :value="todayTotal" />
      </el-col>
      <el-col :span="6">
        <StatCard title="本周总计" :value="current.totalMinutes" />
      </el-col>
      <el-col :span="6">
        <StatCard title="本周日均" :value="dailyAverage" />
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-card__title">较上周</div>
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

    <el-row :gutter="20">
      <!-- 本周逐日节奏 -->
      <el-col :span="14">
        <div class="chart-container">
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div class="chart-container__title">本周逐日用时</div>
            <el-button size="small" text type="primary" @click="$router.push('/report/weekly')">
              查看完整周报 →
            </el-button>
          </div>
          <DailyCompareChart
            v-if="hasAnyRecord"
            :labels="dayLabels"
            :current="dailySeries.current"
            :previous="dailySeries.previous"
          />
          <el-empty v-else description="本周还没有记录" :image-size="80">
            <el-button type="primary" @click="$router.push('/entry')">开始记录</el-button>
          </el-empty>
        </div>
      </el-col>

      <!-- 本周构成 + 记录完整度 -->
      <el-col :span="10">
        <div class="chart-container">
          <div class="chart-container__title">本周时间构成</div>
          <CategoryPieChart
            v-if="hasAnyRecord"
            :data="categoryPie"
            title=""
          />
          <el-empty v-else description="暂无数据" :image-size="80" />
        </div>
        <div class="chart-container">
          <div class="chart-container__title">记录完整度</div>
          <div style="display: flex; align-items: baseline; gap: 8px">
            <span style="font-size: 26px; font-weight: 700">{{ activeDayCount }}</span>
            <span style="color: #909399">/ 7 天有记录</span>
          </div>
          <el-progress
            :percentage="Math.round(activeDayCount / 7 * 100)"
            :stroke-width="10"
            style="margin-top: 10px"
          />
          <div v-if="settlement" style="margin-top: 12px">
            <el-tag type="success" size="small">本周已结存</el-tag>
          </div>
          <div v-else style="margin-top: 12px">
            <el-tag type="info" size="small">本周尚未结存</el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近周结归档 -->
    <div class="chart-container">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div class="chart-container__title">最近周结</div>
        <el-button size="small" text type="primary" @click="$router.push('/report/archive')">
          全部归档 →
        </el-button>
      </div>
      <el-table :data="recentSettlements" size="small" stripe border v-if="recentSettlements.length > 0">
        <el-table-column label="周期" width="200">
          <template #default="{ row }">
            {{ row.year }} 年第 {{ row.weekNumber }} 周
            <span style="color: #909399; font-size: 12px">（{{ row.weekStart }}~）</span>
          </template>
        </el-table-column>
        <el-table-column label="总计" width="120">
          <template #default="{ row }">{{ formatMinutes(row.summary.totalMinutes) }}</template>
        </el-table-column>
        <el-table-column label="I类时间" width="120">
          <template #default="{ row }">{{ formatMinutes(row.summary.categoryTotals['I类时间'] || 0) }}</template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-empty v-else description="还没有周结记录，周五或周初做一次周结即可归档" :image-size="60" />
    </div>

    <!-- 快速入口 -->
    <div class="chart-container">
      <div class="chart-container__title">快捷入口</div>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/entry')">记录时间</el-button>
        <el-button @click="$router.push('/report/weekly')">本周周报</el-button>
        <el-button @click="$router.push('/report/monthly')">月报</el-button>
        <el-button @click="$router.push('/report/year')">年报</el-button>
        <el-button @click="$router.push('/report/distribution')">时间分布</el-button>
        <el-button @click="$router.push('/import')">导入历史数据</el-button>
      </el-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useTimeLogStore } from '@/stores/timeLog'
import { useWeekReport } from '@/composables/useWeekReport'
import { formatMinutes } from '@/utils/timeUtils'
import { ALL_CATEGORIES } from '@/types'
import StatCard from '@/components/common/StatCard.vue'
import CategoryPieChart from '@/components/charts/CategoryPieChart.vue'
import DailyCompareChart from '@/components/charts/DailyCompareChart.vue'

const store = useTimeLogStore()
const {
  weekRange,
  dayLabels,
  current,
  previous,
  hasAnyRecord,
  dailySeries,
  activeDayCount,
  dailyAverage,
  totalChangeRate,
  settlement,
  settlements,
  categorySeries,
} = useWeekReport()

const todayTotal = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return store.records.filter(r => r.date === today).reduce((s, r) => s + r.duration, 0)
})

const categoryPie = computed(() =>
  ALL_CATEGORIES
    .map((name, i) => ({ name, value: categorySeries.value.current[i] || 0 }))
    .filter(d => d.value > 0),
)

const recentSettlements = computed(() => settlements.value.slice(0, 5))

function rateClass(v: number) {
  if (v > 0) return 'rate-up'
  if (v < 0) return 'rate-down'
  return 'rate-flat'
}

onMounted(async () => {
  await store.loadAll()
})
</script>
