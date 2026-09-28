<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="default">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-form-item label="日期" prop="date">
          <el-date-picker v-model="form.date" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" format="HH:mm" value-format="HH:mm" placeholder="开始时间" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" format="HH:mm" value-format="HH:mm" placeholder="结束时间" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in ALL_CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="事件" prop="event">
          <el-select v-model="form.event" placeholder="选择事件" style="width: 100%">
            <el-option v-for="e in availableEvents" :key="e" :label="e" :value="e" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="备注">
          <el-input v-model="form.note" placeholder="可选备注" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { TimeLogRecord, TimeCategory, EventName } from '@/types'
import { ALL_CATEGORIES, L1_EVENTS, L2_EVENTS } from '@/types'
import { calcDuration, getWeekNumber, getYear } from '@/utils/timeUtils'

const props = defineProps<{
  editData?: TimeLogRecord | null
}>()

const emit = defineEmits<{
  submit: [record: TimeLogRecord]
}>()

const formRef = ref<FormInstance>()

const form = ref({
  date: '',
  startTime: '',
  endTime: '',
  category: '' as TimeCategory | '',
  event: '' as EventName | '',
  note: '',
})

const rules: FormRules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  event: [{ required: true, message: '请选择事件', trigger: 'change' }],
}

const availableEvents = computed(() => {
  if (form.value.category === 'I类时间') return L1_EVENTS
  if (form.value.category === 'II类时间') return L2_EVENTS
  return [...L1_EVENTS, ...L2_EVENTS]
})

// 切换分类时重置事件
watch(() => form.value.category, () => {
  form.value.event = ''
})

// 编辑模式填充
watch(() => props.editData, (val) => {
  if (val) {
    form.value = {
      date: val.date,
      startTime: val.startTime,
      endTime: val.endTime,
      category: val.category,
      event: val.event,
      note: val.note || '',
    }
  }
}, { immediate: true })

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    const duration = calcDuration(form.value.startTime, form.value.endTime)
    const record: TimeLogRecord = {
      id: props.editData?.id,
      date: form.value.date,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      duration,
      category: form.value.category as TimeCategory,
      event: form.value.event as EventName,
      weekNumber: getWeekNumber(form.value.date),
      year: getYear(form.value.date),
      note: form.value.note || undefined,
    }
    emit('submit', record)
  })
}

function handleReset() {
  formRef.value?.resetFields()
}
</script>
