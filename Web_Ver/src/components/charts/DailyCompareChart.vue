<template>
  <v-chart :option="chartOption" autoresize style="height: 320px" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'

const props = withDefaults(defineProps<{
  labels: string[]
  current: number[]
  previous: number[]
  currentLabel?: string
  previousLabel?: string
}>(), {
  currentLabel: '本周',
  previousLabel: '上周',
})

const { ACCENT_COLORS } = useChartTheme()

const chartOption = computed(() => ({
  color: [ACCENT_COLORS[4], ACCENT_COLORS[2]],
  tooltip: { trigger: 'axis', formatter: (params: any) => {
    const lines = [params[0].axisValue]
    for (const p of params) {
      const v = p.value as number
      lines.push(`${p.marker}${p.seriesName}: ${Math.floor(v / 60)}小时${v % 60}分`)
    }
    return lines.join('<br/>')
  } },
  legend: { bottom: 0, data: [props.currentLabel, props.previousLabel] },
  grid: { left: '3%', right: '4%', top: 24, bottom: '16%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: props.labels },
  yAxis: { type: 'value', name: '分钟' },
  series: [
    {
      name: props.currentLabel,
      type: 'line',
      data: props.current,
      smooth: true,
      areaStyle: { opacity: 0.25 },
      lineStyle: { width: 2 },
    },
    {
      name: props.previousLabel,
      type: 'line',
      data: props.previous,
      smooth: true,
      lineStyle: { width: 2, type: 'dashed' },
    },
  ],
}))
</script>
