// 时间分类
export type TimeCategory = 'I类时间' | 'II类时间'

// I类时间事件
export type L1Event = '健康' | '学习' | '阅读' | '产出' | '投资' | '社交'

// II类时间事件
export type L2Event = '思考' | '整理' | '兴趣'

// 所有事件
export type EventName = L1Event | L2Event

// I类时间对应的事件列表
export const L1_EVENTS: L1Event[] = ['健康', '学习', '阅读', '产出', '投资', '社交']

// II类时间对应的事件列表
export const L2_EVENTS: L2Event[] = ['思考', '整理', '兴趣']

// 所有事件列表
export const ALL_EVENTS: EventName[] = [...L1_EVENTS, ...L2_EVENTS]

// 所有分类列表
export const ALL_CATEGORIES: TimeCategory[] = ['I类时间', 'II类时间']

// 时间日志记录
export interface TimeLogRecord {
  id?: number
  date: string          // 'YYYY-MM-DD'
  startTime: string     // 'HH:mm'
  endTime: string       // 'HH:mm'
  duration: number      // 历时分钟
  category: TimeCategory
  event: EventName
  weekNumber?: number   // ISO 周数
  year?: number
  note?: string
}

// 图表数据集
export interface ChartDataSet {
  labels: string[]
  datasets: { name: string; values: number[] }[]
}

// 周对比数据
export interface WeekCompareData {
  currentWeek: WeekAggregation
  previousWeek: WeekAggregation
  diff: {
    categoryDiff: Record<string, number>
    categoryRate: Record<string, number>
    eventDiff: Record<string, number>
    eventRate: Record<string, number>
  }
}

// 周聚合数据
export interface WeekAggregation {
  categoryTotals: Record<string, number>
  eventTotals: Record<string, number>
  dailyTotals: Record<string, number>
  totalMinutes: number
}

// 单周系列数据（用于图表穿插对比）
export interface WeekSeriesData {
  current: number[]
  previous: number[]
}

// 周结报告（将某一周的数据固化为存档）
export interface WeekSettlementRecord {
  id?: number
  year: number
  weekNumber: number
  weekStart: string          // 'YYYY-MM-DD'
  weekEnd: string            // 'YYYY-MM-DD'
  summary: WeekAggregation   // 结存时的周聚合快照
  settledAt: string          // 结存时间 ISO 字符串
  note?: string
}

// 月报数据
export interface MonthlyReportData {
  month: string
  categoryTotals: Record<string, number>
  l1EventTotals: Record<string, number>
  l2EventTotals: Record<string, number>
  totalMinutes: number
}

// 时间分布数据（24小时）
export interface DistributionData {
  // 横轴为时间刻度（0-1439 对应 00:00-23:59），纵轴为分钟计数
  timeSum: number[]
  categorySums: Record<string, number[]>
  eventSums: Record<string, number[]>
}

// 年度聚合数据
export interface YearAggregation {
  year: number
  monthCategoryTotals: Record<string, Record<string, number>>  // month -> category -> minutes
  eventTotals: Record<string, number>
  categoryTotals: Record<string, number>
  monthDailyAvg: Record<string, number>  // 月均每日用时
  totalMinutes: number
  recordCount: number
  activeDays: number
}

// 导入结果
export interface ImportResult {
  totalRows: number
  successRows: number
  failedRows: number
  errors: string[]
}

// 日志筛选条件
export interface LogFilter {
  dateRange?: [string, string]
  category?: TimeCategory
  event?: EventName
  keyword?: string
}
