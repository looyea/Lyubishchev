<template>
  <div>
    <div class="page-header">
      <h2>全部记录</h2>
      <p>查询与修正历史明细；日常登记请使用「时间记录」页</p>
    </div>

    <div class="chart-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
        <div class="chart-container__title" style="margin-bottom: 0">
          明细列表
          <el-tag size="small" type="info" style="margin-left: 8px">{{ store.totalCount }} 条</el-tag>
        </div>
        <el-space>
          <el-button size="small" @click="$router.push('/entry')">去登记</el-button>
          <el-button
            v-if="selectedRecords.length > 0"
            type="danger"
            size="small"
            @click="handleBulkDelete"
          >
            批量删除 ({{ selectedRecords.length }})
          </el-button>
        </el-space>
      </div>

      <LogFilterBar @search="handleSearch" @reset="handleReset" />

      <div style="margin-top: 12px">
        <LogTable
          :data="pagedRecords"
          :loading="store.loading"
          @edit="handleEdit"
          @delete="handleDelete"
          @selection-change="handleSelectionChange"
        />
        <el-pagination
          v-if="store.records.length > pageSize"
          v-model:current-page="pageNo"
          :page-size="pageSize"
          :total="store.records.length"
          layout="prev, pager, next, jumper, total"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="修正记录" width="720px">
      <LogForm v-if="editingRecord" :edit-data="editingRecord" @submit="handleFormSubmit" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTimeLogStore } from '@/stores/timeLog'
import type { TimeLogRecord, LogFilter } from '@/types'
import LogForm from '@/components/log/LogForm.vue'
import LogTable from '@/components/log/LogTable.vue'
import LogFilterBar from '@/components/log/LogFilter.vue'

const store = useTimeLogStore()

const pageNo = ref(1)
const pageSize = 50

const pagedRecords = computed(() => {
  const start = (pageNo.value - 1) * pageSize
  return store.records.slice(start, start + pageSize)
})

const editVisible = ref(false)
const editingRecord = ref<TimeLogRecord | null>(null)
const selectedRecords = ref<TimeLogRecord[]>([])

onMounted(() => {
  store.loadAll()
})

async function handleFormSubmit(record: TimeLogRecord) {
  try {
    if (record.id) {
      await store.updateRecord(record.id, record)
      ElMessage.success('已修正')
    } else {
      await store.addRecord(record)
      ElMessage.success('新增成功')
    }
    editVisible.value = false
    editingRecord.value = null
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

function handleEdit(record: TimeLogRecord) {
  editingRecord.value = { ...record }
  editVisible.value = true
}

async function handleDelete(id: number) {
  await store.deleteRecord(id)
  ElMessage.success('已删除')
}

async function handleBulkDelete() {
  const ids = selectedRecords.value.map(r => r.id!).filter(Boolean)
  if (ids.length === 0) return
  await store.bulkDelete(ids)
  ElMessage.success(`已删除 ${ids.length} 条记录`)
  selectedRecords.value = []
}

function handleSearch(filter: LogFilter) {
  pageNo.value = 1
  store.query(filter)
}

function handleReset() {
  pageNo.value = 1
  store.loadAll()
}

function handleSelectionChange(records: TimeLogRecord[]) {
  selectedRecords.value = records
}
</script>
