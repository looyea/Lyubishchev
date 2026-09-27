<template>
  <v-chart :option="chartOption" autoresize style="height: 420px" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'
import type { MonthlyReportData } from '@/types'

const props = defineProps<{
  data: MonthlyReportData[]
  level: 'l1' | 'l2'
  title?: string
}>()

const { getRadarOption } = useChartTheme()

const chartOption = computed(() => {
  const totals = props.level === 'l1'
    ? props.data.map(d => d.l1EventTotals)
    : props.data.map(d => d.l2EventTotals)

  const events = props.level === 'l1'
    ? ['健康', '学习', '阅读', '产出', '投资', '社交']
    : ['思考', '整理', '兴趣']

  // 计算每个事件的最大值用于设定雷达图刻度
  const maxValues = events.map(evt =>
    Math.max(...totals.map(t => t[evt] || 0))
  )

  const indicators = events.map((evt, i) => ({
    name: evt,
    max: Math.ceil(maxValues[i] * 1.2) || 100,
  }))

  const seriesData = props.data.map(d => ({
    name: d.month,
    values: events.map(evt => {
      const t = props.level === 'l1' ? d.l1EventTotals : d.l2EventTotals
      return t[evt] || 0
    }),
  }))

  const title = props.title || (props.level === 'l1' ? 'I类时间事件分布' : 'II类时间事件分布')
  return getRadarOption(title, indicators, seriesData)
})
</script>
