<template>
  <v-chart :option="chartOption" autoresize style="height: 400px" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'
import type { DistributionData } from '@/types'
import { minutesToTimeLabel } from '@/utils/timeUtils'

const props = defineProps<{
  data: DistributionData
  showCategories?: boolean
  title?: string
}>()

const { getLineOption } = useChartTheme()

const chartOption = computed(() => {
  // 生成横轴标签（每小时一个）
  const xLabels: string[] = []
  for (let i = 0; i < 1440; i += 60) {
    xLabels.push(minutesToTimeLabel(i))
  }

  // 降采样：每60分钟取一个点，避免数据过密
  const step = 60
  const sampledData: { name: string; data: number[] }[] = []

  if (props.showCategories) {
    for (const [cat, values] of Object.entries(props.data.categorySums)) {
      const sampled = []
      for (let i = 0; i < values.length; i += step) {
        sampled.push(values[i])
      }
      sampledData.push({ name: cat, data: sampled })
    }
  } else {
    const sampled = []
    for (let i = 0; i < props.data.timeSum.length; i += step) {
      sampled.push(props.data.timeSum[i])
    }
    sampledData.push({ name: '总计', data: sampled })

    for (const [evt, values] of Object.entries(props.data.eventSums)) {
      const sampled = []
      for (let i = 0; i < values.length; i += step) {
        sampled.push(values[i])
      }
      sampledData.push({ name: evt, data: sampled })
    }
  }

  return getLineOption(props.title || '24小时时间分布', xLabels, sampledData)
})
</script>
