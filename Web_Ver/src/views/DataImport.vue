<template>
  <div>
    <div class="page-header">
      <h2>数据导入</h2>
      <p>上传 Excel 或 CSV 文件，解析并导入时间日志数据</p>
    </div>

    <div class="chart-container">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls,.csv"
        :on-change="handleFileChange"
        :on-remove="handleRemove"
      >
        <el-icon style="font-size: 48px; color: #909399;"><Upload /></el-icon>
        <div style="margin-top: 8px">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">
            支持 .xlsx / .xls / .csv 格式，每个 Sheet 视为一个月的数据
          </div>
        </template>
      </el-upload>

      <div v-if="previewData.length > 0" style="margin-top: 20px">
        <h4 style="margin-bottom: 12px">
          数据预览（前 10 条，共 {{ parseResult.totalRows }} 行）
        </h4>
        <el-table :data="previewData" stripe border size="small" max-height="300">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="startTime" label="开始" width="80" />
          <el-table-column prop="endTime" label="结束" width="80" />
          <el-table-column prop="duration" label="时长" width="80" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="event" label="事件" width="80" />
          <el-table-column prop="note" label="备注" />
        </el-table>

        <el-alert
          v-if="parseResult.successRows > 0"
          :title="`解析成功: ${parseResult.successRows} 行，失败: ${parseResult.failedRows} 行`"
          :type="parseResult.failedRows > 0 ? 'warning' : 'success'"
          show-icon
          style="margin-top: 12px"
        />

        <el-alert
          v-for="(err, i) in parseResult.errors.slice(0, 5)"
          :key="i"
          :title="err"
          type="error"
          :closable="false"
          style="margin-top: 4px"
        />

        <div style="margin-top: 16px">
          <el-button type="primary" :loading="importing" @click="handleImport">
            确认导入 {{ parseResult.successRows }} 条记录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { TimeLogRecord, ImportResult } from '@/types'
import { parseExcelFile, parseCSVFile } from '@/utils/dataParser'
import { useTimeLogStore } from '@/stores/timeLog'

const store = useTimeLogStore()
const uploadRef = ref()
const importing = ref(false)

const parsedRecords = ref<TimeLogRecord[]>([])
const previewData = ref<TimeLogRecord[]>([])
const parseResult = ref<ImportResult>({ totalRows: 0, successRows: 0, failedRows: 0, errors: [] })

async function handleFileChange(file: any) {
  const rawFile = file.raw as File
  try {
    let result
    if (rawFile.name.endsWith('.csv')) {
      result = await parseCSVFile(rawFile)
    } else {
      result = await parseExcelFile(rawFile)
    }
    parsedRecords.value = result.records
    previewData.value = result.records.slice(0, 10)
    parseResult.value = result.result
  } catch (e: any) {
    ElMessage.error(`文件解析失败: ${e.message}`)
  }
}

function handleRemove() {
  parsedRecords.value = []
  previewData.value = []
  parseResult.value = { totalRows: 0, successRows: 0, failedRows: 0, errors: [] }
}

async function handleImport() {
  if (parsedRecords.value.length === 0) return
  importing.value = true
  try {
    await store.bulkInsert(parsedRecords.value)
    ElMessage.success(`成功导入 ${parsedRecords.value.length} 条记录`)
    parsedRecords.value = []
    previewData.value = []
    parseResult.value = { totalRows: 0, successRows: 0, failedRows: 0, errors: [] }
    uploadRef.value?.clearFiles()
  } catch (e: any) {
    ElMessage.error(`导入失败: ${e.message}`)
  } finally {
    importing.value = false
  }
}
</script>
