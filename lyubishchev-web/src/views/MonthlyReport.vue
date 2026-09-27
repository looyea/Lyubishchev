<template>
  <div>
    <div class="page-header">
      <h2>月度报告</h2>
      <p>按月统计时间分类和事件分布</p>
    </div>

    <!-- 参数选择 -->
    <div class="chart-container">
      <el-form :inline="true" size="default">
        <el-form-item label="年份">
          <el-select v-model="selectedYear" placeholder="选择年份" @change="handleYearChange">
            <el-option v-for="y in availableYears" :key="y" :label="y" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-checkbox-group v-model="selectedMonths">
            <el-checkbox v-for="m in availableMonths" :key="m" :label="m" :value="m">{{ m }}月</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="generateReport">生成报告</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表区域 -->
    <div v-if="reportData.length > 0" id="monthly-report">
      <!-- 时间分类柱状图 -->
      <div class="chart-container">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div class="chart-container__title">时间分类统计</div>
          <ExportButton target-id="monthly-report" file-name="月度报告" />
        </div>
        <TimeBarChart :data="reportData" />
      </div>

      <!-- I类事件雷达图 -->
      <div class="chart-container">
        <div class="chart-container__title">I类时间事件分布</div>
        <EventRadarChart :data="reportData" level="l1" />
      </div>

      <!-- II类事件雷达图 -->
      <div class="chart-container">
        <div class="chart-container__title">II类时间事件分布</div>
        <EventRadarChart :data="reportData" level="l2" />
      </div>

      <!-- 数据表格 -->
      <div class="chart-container">
        <div class="chart-container__title">数据明细</div>
        <el-table :data="tableData" stripe border>
          <el-table-column prop="month" label="月份" width="100" />
          <el-table-column prop="totalMinutes" label="总时长(分钟)" width="120" />
          <el-table-column v-for="cat in ALL_CATEGORIES" :key="cat" :label="cat" width="120">
            <template #default="{ row }">{{ row.categoryTotals[cat] || 0 }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-empty v-else description="请选择年份和月份，点击生成报告" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTimeLogStore } from '@/stores/timeLog'
import { useReportGenerator } from '@/composables/useReportGenerator'
import type { MonthlyReportData } from '@/types'
import { ALL_CATEGORIES } from '@/types'
import TimeBarChart from '@/components/charts/TimeBarChart.vue'
import EventRadarChart from '@/components/charts/EventRadarChart.vue'
import ExportButton from '@/components/common/ExportButton.vue'

const store = useTimeLogStore()
const { generateMonthlyReport, availableYears, getAvailableMonths } = useReportGenerator()

const selectedYear = ref(new Date().getFullYear())
const selectedMonths = ref<number[]>([])

const availableMonths = computed(() => getAvailableMonths(selectedYear.value))

const reportData = ref<MonthlyReportData[]>([])

const tableData = computed(() => reportData.value)

function handleYearChange() {
  selectedMonths.value = []
  reportData.value = []
}

function generateReport() {
  if (selectedMonths.value.length === 0) return
  reportData.value = generateMonthlyReport(selectedYear.value, selectedMonths.value)
}

onMounted(() => {
  store.loadAll()
})
</script>
