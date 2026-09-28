import { computed } from 'vue'
import { useTimeLogStore } from '@/stores/timeLog'
import { aggregateByMonths } from '@/utils/dataAggregator'
import type { MonthlyReportData } from '@/types'

export function useReportGenerator() {
  const store = useTimeLogStore()

  /** 生成月报数据 */
  function generateMonthlyReport(year: number, months: number[]): MonthlyReportData[] {
    return aggregateByMonths(store.records, year, months)
  }

  /** 获取可选的年份列表 */
  const availableYears = computed(() => {
    const years = new Set<number>()
    for (const r of store.records) {
      if (r.year) years.add(r.year)
      else {
        const y = parseInt(r.date.substring(0, 4))
        if (!isNaN(y)) years.add(y)
      }
    }
    return Array.from(years).sort()
  })

  /** 获取指定年份的可用月份 */
  function getAvailableMonths(year: number): number[] {
    const months = new Set<number>()
    for (const r of store.records) {
      const y = parseInt(r.date.substring(0, 4))
      if (y === year) {
        months.add(parseInt(r.date.substring(5, 7)))
      }
    }
    return Array.from(months).sort((a, b) => a - b)
  }

  return {
    generateMonthlyReport,
    availableYears,
    getAvailableMonths,
  }
}
