import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { db } from '@/db'
import type { TimeLogRecord, LogFilter } from '@/types'

export const useTimeLogStore = defineStore('timeLog', () => {
  const records = ref<TimeLogRecord[]>([])
  const loading = ref(false)
  const totalCount = ref(0)
  /** 是否已从 IndexedDB 完成过首次全量加载 */
  const loaded = ref(false)

  const filteredRecords = computed(() => records.value)

  /** 从 IndexedDB 加载全部数据 */
  async function loadAll() {
    loading.value = true
    try {
      records.value = await db.timeLogs.toArray()
      totalCount.value = records.value.length
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  /** 仅在尚未加载时加载，供公共头部等轻量消费者使用 */
  async function ensureLoaded() {
    if (!loaded.value) await loadAll()
  }

  /** 按条件查询 */
  async function query(filter: LogFilter = {}) {
    loading.value = true
    try {
      let collection = db.timeLogs.orderBy('date')

      if (filter.dateRange) {
        collection = db.timeLogs
          .where('date')
          .between(filter.dateRange[0], filter.dateRange[1], true, true)
      }

      let result = await collection.toArray()

      if (filter.category) {
        result = result.filter(r => r.category === filter.category)
      }
      if (filter.event) {
        result = result.filter(r => r.event === filter.event)
      }
      if (filter.keyword) {
        const kw = filter.keyword.toLowerCase()
        result = result.filter(r =>
          r.event.includes(kw) || r.category.includes(kw) || (r.note || '').includes(kw)
        )
      }

      records.value = result
      totalCount.value = result.length
    } finally {
      loading.value = false
    }
  }

  /** 批量导入 */
  async function bulkInsert(data: TimeLogRecord[]) {
    await db.timeLogs.bulkAdd(data)
    await loadAll()
  }

  /** 新增单条 */
  async function addRecord(record: TimeLogRecord) {
    await db.timeLogs.add(record)
    await loadAll()
  }

  /** 更新 */
  async function updateRecord(id: number, changes: Partial<TimeLogRecord>) {
    await db.timeLogs.update(id, changes)
    await loadAll()
  }

  /** 删除 */
  async function deleteRecord(id: number) {
    await db.timeLogs.delete(id)
    await loadAll()
  }

  /** 批量删除 */
  async function bulkDelete(ids: number[]) {
    await db.timeLogs.bulkDelete(ids)
    await loadAll()
  }

  /** 清空所有数据 */
  async function clearAll() {
    await db.timeLogs.clear()
    records.value = []
    totalCount.value = 0
    loaded.value = true
  }

  return {
    records,
    loading,
    totalCount,
    loaded,
    filteredRecords,
    loadAll,
    ensureLoaded,
    query,
    bulkInsert,
    addRecord,
    updateRecord,
    deleteRecord,
    bulkDelete,
    clearAll,
  }
})
