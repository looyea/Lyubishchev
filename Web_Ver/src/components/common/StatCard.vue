<template>
  <div class="stat-card">
    <div class="stat-card__title">{{ title }}</div>
    <div>
      <span class="stat-card__value">{{ displayValue }}</span>
      <span class="stat-card__unit">{{ unit }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatMinutes } from '@/utils/timeUtils'

const props = withDefaults(defineProps<{
  title: string
  value: number
  format?: 'minutes' | 'number'
  unit?: string
}>(), {
  format: 'minutes',
  unit: '',
})

const displayValue = computed(() => {
  if (props.format === 'minutes') return formatMinutes(props.value)
  return props.value.toString()
})
</script>
