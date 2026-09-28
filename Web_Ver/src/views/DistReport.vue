<template>
  <div>
    <div class="page-header">
      <h2>时间分布</h2>
      <p>24小时时间分布分析</p>
    </div>

    <!-- 日期范围选择 -->
    <div class="chart-container">
      <el-form :inline="true" size="default">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="展示模式">
          <el-radio-group v-model="showMode">
            <el-radio value="all">全部事件</el-radio>
            <el-radio value="category">仅分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="generateDistribution">生成图表</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表区域 -->
    <div v-if="distData" id="dist-report">
      <div class="chart-container">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div class="chart-container__title">
            {{ dateRange ? `${dateRange[0]} 至 ${dateRange[1]}` : '' }} 时间分布
          </div>
          <ExportButton target-id="dist-report" file-name="时间分布" />
        </div>
        <TimeLineChart
          :data="distData"
          :show-categories="showMode === 'category'"
          :title="''"
        />
      </div>

      <!-- 统计摘要 -->
      <div class="chart-container">
        <div class="chart-container__title">时段统计</div>
        <el-row :gutter="16">
          <el-col :span="6" v-for="cat in ALL_CATEGORIES" :key="cat">
            <StatCard
              :title="cat"
              :value="categorySummary[cat] || 0"
            />
          </el-col>
          <el-col :span="6">
            <StatCard title="总计" :value="totalMinutes" />
          </el-col>
        </el-row>
      </div>
    </div>

    <el-empty v-else description="请选择日期范围，点击生成图表" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTimeLogStore } from '@/stores/timeLog'
import { aggregateByDaytime } from '@/utils/dataAggregator'
import type { DistributionData } from '@/types'
import { ALL_CATEGORIES } from '@/types'
import TimeLineChart from '@/components/charts/TimeLineChart.vue'
import StatCard from '@/components/common/StatCard.vue'
import ExportButton from '@/components/common/ExportButton.vue'

const store = useTimeLogStore()

const dateRange = ref<[string, string] | null>(null)
const showMode = ref<'all' | 'category'>('all')
const distData = ref<DistributionData | null>(null)

const categorySummary = computed(() => {
  if (!distData.value) return {}
  const result: Record<string, number> = {}
  for (const [cat, values] of Object.entries(distData.value.categorySums)) {
    result[cat] = values.reduce((s, v) => s + v, 0)
  }
  return result
})

const totalMinutes = computed(() => {
  if (!distData.value) return 0
  return distData.value.timeSum.reduce((s, v) => s + v, 0)
})

function generateDistribution() {
  if (!dateRange.value) return
  distData.value = aggregateByDaytime(store.records, dateRange.value[0], dateRange.value[1])
}

onMounted(() => {
  store.loadAll()
})
</script>
