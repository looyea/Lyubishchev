<template>
  <el-date-picker
    v-model="dateRange"
    type="daterange"
    range-separator="至"
    start-placeholder="开始日期"
    end-placeholder="结束日期"
    format="YYYY-MM-DD"
    value-format="YYYY-MM-DD"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  change: [value: [string, string] | null]
}>()

const dateRange = ref<[string, string] | null>(null)

function handleChange(val: [string, string] | null) {
  emit('change', val)
}
</script>
