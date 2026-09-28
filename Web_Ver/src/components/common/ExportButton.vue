<template>
  <el-button-group>
    <el-button size="small" @click="handleExportPNG">
      <el-icon><Picture /></el-icon> 导出PNG
    </el-button>
    <el-button size="small" @click="handleExportPDF">
      <el-icon><Document /></el-icon> 导出PDF
    </el-button>
  </el-button-group>
</template>

<script setup lang="ts">
import { Picture, Document } from '@element-plus/icons-vue'
import { exportToPNG, exportToPDF } from '@/utils/exportUtils'

const props = withDefaults(defineProps<{
  targetId: string
  fileName?: string
}>(), {
  fileName: 'chart',
})

function handleExportPNG() {
  exportToPNG(props.targetId, props.fileName)
}

function handleExportPDF() {
  exportToPDF(props.targetId, props.fileName)
}
</script>
