<template>
  <v-chart :option="chartOption" autoresize style="height: 400px" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'
import type { MonthlyReportData } from '@/types'

const props = defineProps<{
  data: MonthlyReportData[]
  title?: string
}>()

const { getBarOption } = useChartTheme()

const chartOption = computed(() => {
  const categories = ['I类时间', 'II类时间']
  const seriesData = props.data.map(d => ({
    name: d.month,
    data: categories.map(c => d.categoryTotals[c] || 0),
  }))

  return getBarOption(props.title || '时间分类统计', categories, seriesData)
})
</script>
