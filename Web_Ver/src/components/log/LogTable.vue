<template>
  <el-table
    :data="data"
    v-loading="loading"
    stripe
    border
    style="width: 100%"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="50" />
    <el-table-column prop="date" label="日期" width="120" sortable />
    <el-table-column prop="startTime" label="开始" width="80" />
    <el-table-column prop="endTime" label="结束" width="80" />
    <el-table-column prop="duration" label="时长(分钟)" width="100" sortable>
      <template #default="{ row }">
        <el-tag size="small">{{ row.duration }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="category" label="分类" width="100">
      <template #default="{ row }">
        <el-tag :type="row.category === 'I类时间' ? 'success' : 'warning'" size="small">
          {{ row.category }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="event" label="事件" width="80" />
    <el-table-column prop="note" label="备注" min-width="120" show-overflow-tooltip />
    <el-table-column label="操作" width="150" fixed="right">
      <template #default="{ row }">
        <el-button size="small" type="primary" link @click="$emit('edit', row)">编辑</el-button>
        <el-popconfirm title="确定删除此条记录？" @confirm="$emit('delete', row.id)">
          <template #reference>
            <el-button size="small" type="danger" link>删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import type { TimeLogRecord } from '@/types'

defineProps<{
  data: TimeLogRecord[]
  loading?: boolean
}>()

const emit = defineEmits<{
  edit: [record: TimeLogRecord]
  delete: [id: number]
  selectionChange: [records: TimeLogRecord[]]
}>()

function handleSelectionChange(records: TimeLogRecord[]) {
  emit('selectionChange', records)
}
</script>
