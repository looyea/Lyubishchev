<template>
  <div>
    <!-- 双轴对比柱状图：本周/上周柱状 + 变化率折线 -->
    <v-chart v-if="showBar" :option="barOption" autoresize style="height: 360px" />
    <!-- 雷达叠加对比图 -->
    <v-chart v-if="showRadar" :option="radarOption" autoresize style="height: 380px" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'

const props = withDefaults(defineProps<{
  /** 参与对比的维度名称，如 ['I类时间','II类时间'] 或 ['健康','学习',...] */
  items: string[]
  /** 本周各维度数值 */
  current: number[]
  /** 上周各维度数值 */
  previous: number[]
  title?: string
  showBar?: boolean
  showRadar?: boolean
  currentLabel?: string
  previousLabel?: string
}>(), {
  showBar: true,
  showRadar: false,
  currentLabel: '本周',
  previousLabel: '上周',
})

const { ACCENT_COLORS } = useChartTheme()

function rate(curr: number, prev: number): number {
  if (prev === 0) return curr > 0 ? 100 : 0
  return Math.round(((curr - prev) / prev) * 100)
}

const barOption = computed(() => {
  const rates = props.items.map((_, i) => rate(props.current[i] ?? 0, props.previous[i] ?? 0))
  return {
    color: [ACCENT_COLORS[4], ACCENT_COLORS[2], ACCENT_COLORS[5]],
    title: props.title
      ? { text: props.title, left: 'center', textStyle: { fontFamily: '"Microsoft YaHei"', fontSize: 14 } }
      : undefined,
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, data: [props.currentLabel, props.previousLabel, '变化率'] },
    grid: { left: '3%', right: '5%', top: props.title ? 56 : 24, bottom: '14%', containLabel: true },
    xAxis: { type: 'category', data: props.items },
    yAxis: [
      { type: 'value', name: '分钟', position: 'left' },
      { type: 'value', name: '变化率', position: 'right', axisLabel: { formatter: '{value}%' } },
    ],
    series: [
      { name: props.currentLabel, type: 'bar', data: props.current, barGap: '10%' },
      { name: props.previousLabel, type: 'bar', data: props.previous },
      {
        name: '变化率',
        type: 'line',
        yAxisIndex: 1,
        data: rates,
        lineStyle: { width: 2, type: 'dashed' },
        label: { show: true, formatter: '{c}%', fontSize: 10 },
      },
    ],
  }
})

const radarOption = computed(() => {
  // 每个维度的刻度取两周中的较大值并留白
  const maxValues = props.items.map((_, i) =>
    Math.max(props.current[i] ?? 0, props.previous[i] ?? 0) * 1.2 || 60,
  )
  return {
    color: [ACCENT_COLORS[4], ACCENT_COLORS[2]],
    title: props.title
      ? { text: props.title, left: 'center', textStyle: { fontFamily: '"Microsoft YaHei"', fontSize: 14 } }
      : undefined,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, data: [props.currentLabel, props.previousLabel] },
    radar: {
      indicator: props.items.map((name, i) => ({ name, max: maxValues[i] })),
      shape: 'polygon',
      splitNumber: 5,
      radius: '60%',
      axisName: { color: '#333', fontFamily: '"Microsoft YaHei"' },
      splitLine: { lineStyle: { type: 'dashed' } },
    },
    series: [{
      type: 'radar',
      data: [
        {
          name: props.currentLabel,
          value: props.current,
          areaStyle: { opacity: 0.35 },
          lineStyle: { width: 2 },
        },
        {
          name: props.previousLabel,
          value: props.previous,
          areaStyle: { opacity: 0.2 },
          lineStyle: { width: 2, type: 'dashed' },
        },
      ],
    }],
  }
})
</script>
