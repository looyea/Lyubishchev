<template>
  <v-chart :option="chartOption" autoresize style="height: 400px" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { useChartTheme } from '@/composables/useChartTheme'

const props = defineProps<{
  data: { name: string; value: number }[]
  title?: string
}>()

const { getPieOption } = useChartTheme()

const chartOption = computed(() => {
  return getPieOption(props.title || '时间分类占比', props.data)
})
</script>
