import type {
  TimeLogRecord,
  MonthlyReportData,
  DistributionData,
  WeekAggregation,
  WeekCompareData,
  YearAggregation,
} from '@/types'
import { ALL_CATEGORIES, ALL_EVENTS, L1_EVENTS, L2_EVENTS } from '@/types'
import { timeToMinutes, yearMonthList } from './timeUtils'

/**
 * 按月聚合数据 —— 对应原 MonthlyReport DataProcessor 的 pivot 逻辑
 */
export function aggregateByMonth(
  records: TimeLogRecord[],
  year: number,
  month: number
): MonthlyReportData {
  const monthStr = `${year}-${String(month).padStart(2, '0')}`
  const filtered = records.filter(r => r.date.startsWith(monthStr))

  const categoryTotals: Record<string, number> = {}
  const l1EventTotals: Record<string, number> = {}
  const l2EventTotals: Record<string, number> = {}
  let totalMinutes = 0

  for (const cat of ALL_CATEGORIES) categoryTotals[cat] = 0
  for (const evt of L1_EVENTS) l1EventTotals[evt] = 0
  for (const evt of L2_EVENTS) l2EventTotals[evt] = 0

  for (const r of filtered) {
    totalMinutes += r.duration
    categoryTotals[r.category] = (categoryTotals[r.category] || 0) + r.duration
    if (L1_EVENTS.includes(r.event as any)) {
      l1EventTotals[r.event] = (l1EventTotals[r.event] || 0) + r.duration
    } else {
      l2EventTotals[r.event] = (l2EventTotals[r.event] || 0) + r.duration
    }
  }

  return {
    month: monthStr,
    categoryTotals,
    l1EventTotals,
    l2EventTotals,
    totalMinutes,
  }
}

/**
 * 多月份聚合
 */
export function aggregateByMonths(
  records: TimeLogRecord[],
  year: number,
  months: number[]
): MonthlyReportData[] {
  return months.map(m => aggregateByMonth(records, year, m))
}

/**
 * 24小时时间分布聚合 —— 对应原 DistReport DataProcessor
 * 横轴为分钟刻度 0-1439（对应 00:00-23:59）
 */
export function aggregateByDaytime(
  records: TimeLogRecord[],
  startDate: string,
  endDate: string
): DistributionData {
  const TOTAL_MINUTES = 1440
  const timeSum = new Array(TOTAL_MINUTES).fill(0)
  const categorySums: Record<string, number[]> = {}
  const eventSums: Record<string, number[]> = {}

  for (const cat of ALL_CATEGORIES) {
    categorySums[cat] = new Array(TOTAL_MINUTES).fill(0)
  }
  for (const evt of ALL_EVENTS) {
    eventSums[evt] = new Array(TOTAL_MINUTES).fill(0)
  }

  const filtered = records.filter(r => r.date >= startDate && r.date <= endDate)

  for (const r of filtered) {
    const startMin = timeToMinutes(r.startTime)
    const endMin = timeToMinutes(r.endTime)

    for (let i = startMin; i < endMin && i < TOTAL_MINUTES; i++) {
      timeSum[i] += 1
      categorySums[r.category][i] += 1
      eventSums[r.event][i] += 1
    }
  }

  return { timeSum, categorySums, eventSums }
}

/**
 * 按周聚合数据
 */
export function aggregateByWeek(
  records: TimeLogRecord[],
  startDate: string,
  endDate: string
): WeekAggregation {
  const filtered = records.filter(r => r.date >= startDate && r.date <= endDate)

  const categoryTotals: Record<string, number> = {}
  const eventTotals: Record<string, number> = {}
  const dailyTotals: Record<string, number> = {}
  let totalMinutes = 0

  for (const cat of ALL_CATEGORIES) categoryTotals[cat] = 0
  for (const evt of ALL_EVENTS) eventTotals[evt] = 0

  for (const r of filtered) {
    totalMinutes += r.duration
    categoryTotals[r.category] = (categoryTotals[r.category] || 0) + r.duration
    eventTotals[r.event] = (eventTotals[r.event] || 0) + r.duration
    dailyTotals[r.date] = (dailyTotals[r.date] || 0) + r.duration
  }

  return { categoryTotals, eventTotals, dailyTotals, totalMinutes }
}

/**
 * 周对比分析
 */
export function compareWeeks(
  records: TimeLogRecord[],
  currentStart: string,
  currentEnd: string,
  previousStart: string,
  previousEnd: string
): WeekCompareData {
  const currentWeek = aggregateByWeek(records, currentStart, currentEnd)
  const previousWeek = aggregateByWeek(records, previousStart, previousEnd)

  const categoryDiff: Record<string, number> = {}
  const categoryRate: Record<string, number> = {}
  const eventDiff: Record<string, number> = {}
  const eventRate: Record<string, number> = {}

  for (const cat of ALL_CATEGORIES) {
    const curr = currentWeek.categoryTotals[cat] || 0
    const prev = previousWeek.categoryTotals[cat] || 0
    categoryDiff[cat] = curr - prev
    categoryRate[cat] = prev === 0 ? (curr > 0 ? 100 : 0) : Math.round((curr - prev) / prev * 100)
  }

  for (const evt of ALL_EVENTS) {
    const curr = currentWeek.eventTotals[evt] || 0
    const prev = previousWeek.eventTotals[evt] || 0
    eventDiff[evt] = curr - prev
    eventRate[evt] = prev === 0 ? (curr > 0 ? 100 : 0) : Math.round((curr - prev) / prev * 100)
  }

  return {
    currentWeek,
    previousWeek,
    diff: { categoryDiff, categoryRate, eventDiff, eventRate },
  }
}

/**
 * 按年聚合数据 —— 年度报告
 */
export function aggregateByYear(records: TimeLogRecord[], year: number): YearAggregation {
  const prefix = `${year}-`
  const filtered = records.filter(r => r.date.startsWith(prefix))

  const months = yearMonthList(year)
  const monthCategoryTotals: Record<string, Record<string, number>> = {}
  for (const m of months) {
    monthCategoryTotals[m] = {}
    for (const cat of ALL_CATEGORIES) monthCategoryTotals[m][cat] = 0
  }

  const eventTotals: Record<string, number> = {}
  const categoryTotals: Record<string, number> = {}
  for (const evt of ALL_EVENTS) eventTotals[evt] = 0
  for (const cat of ALL_CATEGORIES) categoryTotals[cat] = 0

  // 按月统计有记录的天数，用于月均每日用时
  const monthActiveDays: Record<string, Set<string>> = {}
  for (const m of months) monthActiveDays[m] = new Set()

  let totalMinutes = 0
  for (const r of filtered) {
    const m = r.date.slice(0, 7)
    totalMinutes += r.duration
    categoryTotals[r.category] = (categoryTotals[r.category] || 0) + r.duration
    eventTotals[r.event] = (eventTotals[r.event] || 0) + r.duration
    if (monthCategoryTotals[m]) {
      monthCategoryTotals[m][r.category] = (monthCategoryTotals[m][r.category] || 0) + r.duration
      monthActiveDays[m].add(r.date)
    }
  }

  const monthDailyAvg: Record<string, number> = {}
  for (const m of months) {
    const mTotal = ALL_CATEGORIES.reduce((s, cat) => s + (monthCategoryTotals[m][cat] || 0), 0)
    const activeDays = monthActiveDays[m].size
    // 以有记录的天数为除数，避免未记录月份被 30 天拉低
    monthDailyAvg[m] = activeDays === 0 ? 0 : Math.round(mTotal / activeDays)
  }

  const allDays = new Set(filtered.map(r => r.date))

  return {
    year,
    monthCategoryTotals,
    eventTotals,
    categoryTotals,
    monthDailyAvg,
    totalMinutes,
    recordCount: filtered.length,
    activeDays: allDays.size,
  }
}

/**
 * 获取仪表盘概览数据
 */
export function getDashboardSummary(records: TimeLogRecord[], date: string) {
  const todayTotal = records.filter(r => r.date === date).reduce((s, r) => s + r.duration, 0)

  // 本周（周一为一周起点）
  const now = new Date(date)
  const dayOfWeek = now.getDay() || 7
  const weekStart = new Date(now)
  weekStart.setDate(now.getDate() - dayOfWeek + 1)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 6)

  const weekStr = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  const weekRecords = records.filter(r => r.date >= weekStr(weekStart) && r.date <= weekStr(weekEnd))
  const weekTotal = weekRecords.reduce((s, r) => s + r.duration, 0)

  // 分类统计
  const categoryTotals: Record<string, number> = {}
  for (const cat of ALL_CATEGORIES) categoryTotals[cat] = 0
  for (const r of weekRecords) {
    categoryTotals[r.category] = (categoryTotals[r.category] || 0) + r.duration
  }

  // 事件统计
  const eventTotals: Record<string, number> = {}
  for (const evt of ALL_EVENTS) eventTotals[evt] = 0
  for (const r of weekRecords) {
    eventTotals[r.event] = (eventTotals[r.event] || 0) + r.duration
  }

  return {
    todayTotal,
    weekTotal,
    weekRange: { start: weekStr(weekStart), end: weekStr(weekEnd) },
    categoryTotals,
    eventTotals,
    recordCount: weekRecords.length,
  }
}
