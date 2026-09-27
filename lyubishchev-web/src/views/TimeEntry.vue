<template>
  <div>
    <div class="page-header">
      <h2>时间记录</h2>
      <p>白天随手记录，做完一件事就登记一条；开始时间自动承接上一条的结束时间</p>
    </div>

    <el-row :gutter="20">
      <!-- 录入区 -->
      <el-col :span="10">
        <div class="chart-container">
          <div class="chart-container__title">
            登记一条
            <el-tag size="small" type="info" style="margin-left: 8px">{{ recordDate }}</el-tag>
          </div>

          <el-form :model="form" label-width="72px" size="default">
            <el-form-item label="日期">
              <el-date-picker
                v-model="recordDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="handleDateChange"
              />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="开始">
                  <el-time-select
                    v-model="form.startTime"
                    start="00:00"
                    step="00:05"
                    end="23:55"
                    placeholder="开始"
                    style="width: 100%"
                    @change="recalcDuration"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束">
                  <el-time-select
                    v-model="form.endTime"
                    :start="form.startTime || '00:00'"
                    step="00:05"
                    end="23:55"
                    placeholder="结束"
                    style="width: 100%"
                    @change="recalcDuration"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="时长">
              <el-tag :type="duration > 0 ? 'success' : 'info'" size="large">
                {{ duration > 0 ? formatMinutes(duration) : '待填写时间' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="分类">
              <el-radio-group v-model="form.category" @change="handleCategoryChange">
                <el-radio-button v-for="c in ALL_CATEGORIES" :key="c" :value="c">{{ c }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="事件">
              <el-select v-model="form.event" placeholder="选择事件" style="width: 100%">
                <el-option v-for="e in eventOptions" :key="e" :label="e" :value="e" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.note" placeholder="可选" clearable />
            </el-form-item>

            <!-- 常用组合快捷按钮 -->
            <el-form-item label="常用">
              <el-space wrap size="small">
                <el-button
                  v-for="q in quickPicks"
                  :key="q.key"
                  size="small"
                  @click="applyQuickPick(q)"
                >
                  {{ q.label }}
                </el-button>
              </el-space>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" style="width: 100%" @click="handleSubmit">
                保存并继续
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- 当天时间轴 -->
      <el-col :span="14">
        <div class="chart-container">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px">
            <div class="chart-container__title" style="margin-bottom: 0">
              {{ recordDate }} 已记录 {{ formatMinutes(dayTotal) }}
            </div>
            <el-space>
              <el-tag size="small" type="success">I类 {{ formatMinutes(dayCategoryTotal('I类时间')) }}</el-tag>
              <el-tag size="small" type="warning">II类 {{ formatMinutes(dayCategoryTotal('II类时间')) }}</el-tag>
            </el-space>
          </div>

          <el-table :data="dayRecords" stripe border size="small" max-height="560">
            <el-table-column prop="startTime" label="开始" width="66" />
            <el-table-column prop="endTime" label="结束" width="66" />
            <el-table-column label="时长" width="84">
              <template #default="{ row }">{{ formatMinutes(row.duration) }}</template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="80">
              <template #default="{ row }">
                <el-tag :type="row.category === 'I类时间' ? 'success' : 'warning'" size="small">
                  {{ row.category }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="event" label="事件" min-width="64" />
            <el-table-column prop="note" label="备注" min-width="90" show-overflow-tooltip />
            <el-table-column label="操作" width="92">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="handleEdit(row)">改</el-button>
                <el-popconfirm title="删除这条记录？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="dayRecords.length === 0" description="今天还没有记录" :image-size="60" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useTimeLogStore } from '@/stores/timeLog'
import { calcDuration, formatMinutes, getWeekNumber, getYear } from '@/utils/timeUtils'
import { ALL_CATEGORIES, L1_EVENTS, L2_EVENTS } from '@/types'
import type { TimeLogRecord, TimeCategory, EventName } from '@/types'

const store = useTimeLogStore()

const recordDate = ref(dayjs().format('YYYY-MM-DD'))
const submitting = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  startTime: '',
  endTime: '',
  category: 'I类时间' as TimeCategory,
  event: '' as EventName | '',
  note: '',
})

const eventOptions = computed(() =>
  form.category === 'I类时间' ? [...L1_EVENTS] : [...L2_EVENTS],
)

/** 当天记录，按开始时间排序 */
const dayRecords = computed(() =>
  store.records
    .filter(r => r.date === recordDate.value)
    .sort((a, b) => a.startTime.localeCompare(b.startTime)),
)

const dayTotal = computed(() =>
  dayRecords.value.reduce((s, r) => s + r.duration, 0),
)

function dayCategoryTotal(cat: TimeCategory) {
  return dayRecords.value.filter(r => r.category === cat).reduce((s, r) => s + r.duration, 0)
}

const duration = computed(() =>
  form.startTime && form.endTime ? calcDuration(form.startTime, form.endTime) : 0,
)

/** 常用组合：分类+事件+时长，一键登记 */
const quickPicks = [
  { key: 'q1', label: '阅读 30分', category: 'I类时间' as TimeCategory, event: '阅读' as EventName, minutes: 30 },
  { key: 'q2', label: '学习 1小时', category: 'I类时间' as TimeCategory, event: '学习' as EventName, minutes: 60 },
  { key: 'q3', label: '产出 2小时', category: 'I类时间' as TimeCategory, event: '产出' as EventName, minutes: 120 },
  { key: 'q4', label: '健康 1小时', category: 'I类时间' as TimeCategory, event: '健康' as EventName, minutes: 60 },
  { key: 'q5', label: '思考 30分', category: 'II类时间' as TimeCategory, event: '思考' as EventName, minutes: 30 },
]

/** 开始时间自动承接上一条的结束时间 */
const lastEndTime = computed(() => {
  const list = dayRecords.value
  return list.length > 0 ? list[list.length - 1].endTime : ''
})

function syncStartTime() {
  if (!form.startTime) {
    form.startTime = lastEndTime.value || dayjs().format('HH:mm')
  }
}

function recalcDuration() {
  // 结束时间早于开始时间时自动清空，避免误记
  if (form.startTime && form.endTime && form.endTime <= form.startTime) {
    form.endTime = ''
  }
}

function handleCategoryChange() {
  form.event = ''
}

function handleDateChange() {
  editingId.value = null
  form.startTime = ''
  form.endTime = ''
  syncStartTime()
}

function applyQuickPick(q: (typeof quickPicks)[number]) {
  form.category = q.category
  form.event = q.event
  const start = form.startTime || lastEndTime.value || dayjs().format('HH:mm')
  form.startTime = start
  form.endTime = dayjs(`${recordDate.value} ${start}`)
    .add(q.minutes, 'minute')
    .format('HH:mm')
}

async function handleSubmit() {
  if (!recordDate.value || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写开始与结束时间')
    return
  }
  if (!form.event) {
    ElMessage.warning('请选择事件')
    return
  }
  const dur = duration.value
  if (dur <= 0) {
    ElMessage.warning('结束时间需晚于开始时间')
    return
  }

  submitting.value = true
  try {
    const payload: TimeLogRecord = {
      date: recordDate.value,
      startTime: form.startTime,
      endTime: form.endTime,
      duration: dur,
      category: form.category,
      event: form.event as EventName,
      weekNumber: getWeekNumber(recordDate.value),
      year: getYear(recordDate.value),
      note: form.note || undefined,
    }

    if (editingId.value) {
      await store.updateRecord(editingId.value, payload)
      editingId.value = null
      ElMessage.success('已更新')
    } else {
      await store.addRecord(payload)
      ElMessage.success(`已记录 ${formatMinutes(dur)}`)
    }

    // 继续登记：开始时间推进到刚才的结束时间
    const nextStart = form.endTime
    form.endTime = ''
    form.note = ''
    form.startTime = nextStart
  } catch (e: any) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

function handleEdit(row: TimeLogRecord) {
  editingId.value = row.id!
  form.startTime = row.startTime
  form.endTime = row.endTime
  form.category = row.category
  form.event = row.event
  form.note = row.note || ''
}

async function handleDelete(id: number) {
  await store.deleteRecord(id)
  ElMessage.success('已删除')
}

onMounted(async () => {
  await store.loadAll()
  syncStartTime()
})
</script>
