<template>
  <div>
    <div class="page-header">
      <h2>历史周结</h2>
      <p>历次结存的周报归档，可查看单周快照与相邻周对比</p>
    </div>

    <div class="chart-container">
      <el-table :data="store.settlements" v-loading="store.loading" stripe border>
        <el-table-column label="周期" width="220">
          <template #default="{ row }">
            <div>{{ row.year }} 年第 {{ row.weekNumber }} 周</div>
            <div style="font-size: 12px; color: #909399">{{ row.weekStart }} ~ {{ row.weekEnd }}</div>
          </template>
        </el-table-column>
        <el-table-column label="总计" width="130">
          <template #default="{ row }">{{ formatMinutes(row.summary.totalMinutes) }}</template>
        </el-table-column>
        <el-table-column label="I类时间" width="120">
          <template #default="{ row }">{{ formatMinutes(row.summary.categoryTotals['I类时间'] || 0) }}</template>
        </el-table-column>
        <el-table-column label="II类时间" width="120">
          <template #default="{ row }">{{ formatMinutes(row.summary.categoryTotals['II类时间'] || 0) }}</template>
        </el-table-column>
        <el-table-column prop="note" label="周备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="结存时间" width="160">
          <template #default="{ row }">{{ dayjs(row.settledAt).format('MM-DD HH:mm') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDetail(row)">查看</el-button>
            <el-popconfirm title="删除该周结记录？（不影响原始日志）" @confirm="store.remove(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!store.loading && store.settlements.length === 0" description="还没有结存过任何一周">
        <el-button type="primary" @click="$router.push('/report/weekly')">去做本周周结</el-button>
      </el-empty>
    </div>

    <!-- 单周详情 -->
    <el-dialog v-model="detailVisible" :title="detailTitle" width="860px" top="6vh">
      <template v-if="currentDetail">
        <!-- 与前一已结存周的对比 -->
        <div class="chart-container" v-if="compareDetail">
          <div class="chart-container__title">与上一结存周对比</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <CompareChart
                :items="ALL_CATEGORIES"
                :current="detailCategoryValues(currentDetail)"
                :previous="detailCategoryValues(compareDetail)"
                :current-label="weekLabel(currentDetail)"
                :previous-label="weekLabel(compareDetail)"
              />
            </el-col>
            <el-col :span="12">
              <CompareChart
                :items="L1_EVENTS"
                :current="detailL1Values(currentDetail)"
                :previous="detailL1Values(compareDetail)"
                :show-bar="false"
                :show-radar="true"
                :current-label="weekLabel(currentDetail)"
                :previous-label="weekLabel(compareDetail)"
              />
            </el-col>
          </el-row>
        </div>

        <div class="chart-container">
          <div class="chart-container__title">事件明细</div>
          <el-table :data="detailTable" stripe border size="small">
            <el-table-column prop="event" label="事件" width="100" />
            <el-table-column prop="category" label="分类" width="110" />
            <el-table-column label="用时" width="130">
              <template #default="{ row }">{{ formatMinutes(row.minutes) }}</template>
            </el-table-column>
            <el-table-column v-if="compareDetail" label="上一结存周" width="130">
              <template #default="{ row }">{{ formatMinutes(row.prevMinutes) }}</template>
            </el-table-column>
            <el-table-column v-if="compareDetail" label="环比" width="110">
              <template #default="{ row }">
                <span :class="row.diff > 0 ? 'rate-up' : row.diff < 0 ? 'rate-down' : 'rate-flat'">
                  {{ row.diff > 0 ? '+' : '' }}{{ row.diff }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="占比" min-width="160">
              <template #default="{ row }">
                <el-progress :percentage="row.percent" :stroke-width="10" />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="chart-container" v-if="currentDetail.note">
          <div class="chart-container__title">周备注</div>
          <div style="white-space: pre-wrap">{{ currentDetail.note }}</div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useSettlementStore } from '@/stores/settlement'
import { formatMinutes } from '@/utils/timeUtils'
import { ALL_CATEGORIES, ALL_EVENTS, L1_EVENTS } from '@/types'
import type { WeekSettlementRecord } from '@/types'
import CompareChart from '@/components/charts/CompareChart.vue'

const store = useSettlementStore()

const detailVisible = ref(false)
const currentDetail = ref<WeekSettlementRecord | null>(null)

const detailTitle = computed(() =>
  currentDetail.value ? weekLabel(currentDetail.value) + ' 周结详情' : '周结详情',
)

/** 在归档列表中定位该周的上一个已结存周（列表按时间倒序） */
const compareDetail = computed<WeekSettlementRecord | null>(() => {
  if (!currentDetail.value) return null
  const list = store.settlements
  const idx = list.findIndex(s => s.id === currentDetail.value!.id)
  return idx >= 0 && idx + 1 < list.length ? list[idx + 1] : null
})

function weekLabel(s: WeekSettlementRecord) {
  return `${s.year}年第${s.weekNumber}周`
}

function detailCategoryValues(s: WeekSettlementRecord) {
  return ALL_CATEGORIES.map(c => s.summary.categoryTotals[c] || 0)
}

function detailL1Values(s: WeekSettlementRecord) {
  return L1_EVENTS.map(e => s.summary.eventTotals[e] || 0)
}

const detailTable = computed(() => {
  if (!currentDetail.value) return []
  const total = currentDetail.value.summary.totalMinutes || 1
  const prevTotals = compareDetail.value?.summary.eventTotals || {}
  return ALL_EVENTS.map(evt => {
    const minutes = currentDetail.value!.summary.eventTotals[evt] || 0
    const prevMinutes = prevTotals[evt] || 0
    return {
      event: evt,
      category: L1_EVENTS.includes(evt as any) ? 'I类时间' : 'II类时间',
      minutes,
      prevMinutes,
      diff: minutes - prevMinutes,
      percent: Math.round((minutes / total) * 100),
    }
  }).filter(r => r.minutes > 0 || r.prevMinutes > 0)
})

function openDetail(row: WeekSettlementRecord) {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  store.loadAll()
})
</script>
