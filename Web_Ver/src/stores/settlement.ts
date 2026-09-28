import { defineStore } from 'pinia'
import { ref } from 'vue'
import { db } from '@/db'
import type { WeekSettlementRecord, WeekAggregation } from '@/types'

/**
 * 周结报告 store —— 管理每周结存的归档
 */
export const useSettlementStore = defineStore('settlement', () => {
  const settlements = ref<WeekSettlementRecord[]>([])
  const loading = ref(false)

  async function loadAll() {
    loading.value = true
    try {
      const all = await db.weekReports.toArray()
      // 按周起始日倒序，最新在前
      settlements.value = all.sort((a, b) => (a.weekStart < b.weekStart ? 1 : -1))
    } finally {
      loading.value = false
    }
  }

  /** 查询某一周是否已结存 */
  async function findByWeek(year: number, weekNumber: number): Promise<WeekSettlementRecord | undefined> {
    return db.weekReports
      .where('[year+weekNumber]')
      .equals([year, weekNumber])
      .first()
  }

  /** 结存（存在则更新快照，不存在则新增） */
  async function settle(payload: {
    year: number
    weekNumber: number
    weekStart: string
    weekEnd: string
    summary: WeekAggregation
    note?: string
  }) {
    const existing = await findByWeek(payload.year, payload.weekNumber)
    const record: WeekSettlementRecord = {
      ...(existing || {}),
      year: payload.year,
      weekNumber: payload.weekNumber,
      weekStart: payload.weekStart,
      weekEnd: payload.weekEnd,
      summary: payload.summary,
      settledAt: new Date().toISOString(),
      note: payload.note,
    }
    await db.weekReports.put(record)
    await loadAll()
    return record
  }

  async function remove(id: number) {
    await db.weekReports.delete(id)
    await loadAll()
  }

  return {
    settlements,
    loading,
    loadAll,
    findByWeek,
    settle,
    remove,
  }
})
