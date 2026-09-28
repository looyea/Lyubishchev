<template>
  <el-form :inline="true" :model="filter" size="default">
    <el-form-item label="日期范围">
      <el-date-picker
        v-model="filter.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    <el-form-item label="分类">
      <el-select v-model="filter.category" placeholder="全部分类" clearable style="width: 140px">
        <el-option v-for="c in ALL_CATEGORIES" :key="c" :label="c" :value="c" />
      </el-select>
    </el-form-item>
    <el-form-item label="事件">
      <el-select v-model="filter.event" placeholder="全部事件" clearable style="width: 120px">
        <el-option v-for="e in ALL_EVENTS" :key="e" :label="e" :value="e" />
      </el-select>
    </el-form-item>
    <el-form-item label="关键词">
      <el-input v-model="filter.keyword" placeholder="搜索备注..." clearable style="width: 160px" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="$emit('search', filter)">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import type { LogFilter } from '@/types'
import { ALL_CATEGORIES, ALL_EVENTS } from '@/types'

const emit = defineEmits<{
  search: [filter: LogFilter]
  reset: []
}>()

const filter = reactive<LogFilter>({})

function handleReset() {
  filter.dateRange = undefined
  filter.category = undefined
  filter.event = undefined
  filter.keyword = undefined
  emit('reset')
}
</script>
