import dayjs from 'dayjs'
import isoWeek from 'dayjs/plugin/isoWeek'

dayjs.extend(isoWeek)

/**
 * 将 "HH:mm" 或 "HH:mm:ss" 格式的时间转换为分钟数（从 00:00 起）
 */
export function timeToMinutes(timeStr: string): number {
  const parts = timeStr.split(':')
  const hours = parseInt(parts[0], 10) || 0
  const minutes = parseInt(parts[1], 10) || 0
  return hours * 60 + minutes
}

/**
 * 计算两个时间之间的分钟差
 */
export function calcDuration(startTime: string, endTime: string): number {
  const start = timeToMinutes(startTime)
  let end = timeToMinutes(endTime)
  if (end <= start) end += 24 * 60 // 跨天情况
  return end - start
}

/**
 * 格式化时间显示（分钟 -> "Xh Ym"）
 */
export function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}分钟`
  if (m === 0) return `${h}小时`
  return `${h}小时${m}分钟`
}

/**
 * 获取 ISO 周数
 */
export function getWeekNumber(dateStr: string): number {
  return dayjs(dateStr).isoWeek()
}

/**
 * 获取年份
 */
export function getYear(dateStr: string): number {
  return dayjs(dateStr).year()
}

/**
 * 获取指定日期所在周的起止日期（周一到周日）
 */
export function getWeekRange(dateStr: string): { start: string; end: string } {
  const d = dayjs(dateStr)
  return {
    start: d.startOf('isoWeek').format('YYYY-MM-DD'),
    end: d.endOf('isoWeek').format('YYYY-MM-DD'),
  }
}

/**
 * 获取上一周的起止日期
 */
export function getPreviousWeekRange(dateStr: string): { start: string; end: string } {
  const d = dayjs(dateStr).subtract(1, 'week')
  return {
    start: d.startOf('isoWeek').format('YYYY-MM-DD'),
    end: d.endOf('isoWeek').format('YYYY-MM-DD'),
  }
}

/**
 * 日期加减天数
 */
export function addWeekDays(dateStr: string, weeks: number): string {
  return dayjs(dateStr).add(weeks, 'week').format('YYYY-MM-DD')
}

/**
 * 生成周一到周日的日期列表
 */
export function weekDateList(weekStart: string): string[] {
  return Array.from({ length: 7 }, (_, i) =>
    dayjs(weekStart).add(i, 'day').format('YYYY-MM-DD')
  )
}

/**
 * 生成周一到周日的中文标签
 */
export function weekDayLabels(weekStart: string): string[] {
  const names = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return weekDateList(weekStart).map((d, i) => `${names[i]} ${dayjs(d).format('MM-DD')}`)
}

/**
 * 获取指定年份的所有月份 'YYYY-MM'
 */
export function yearMonthList(year: number): string[] {
  return Array.from({ length: 12 }, (_, i) => `${year}-${String(i + 1).padStart(2, '0')}`)
}

/**
 * 获取月份的天数
 */
export function daysInMonth(year: number, month: number): number {
  return dayjs(`${year}-${String(month).padStart(2, '0')}-01`).daysInMonth()
}

/**
 * 获取指定月份的所有日期
 */
export function getMonthDates(year: number, month: number): string[] {
  const dates: string[] = []
  const d = dayjs(`${year}-${String(month).padStart(2, '0')}-01`)
  const daysInMonth = d.daysInMonth()
  for (let i = 0; i < daysInMonth; i++) {
    dates.push(d.add(i, 'day').format('YYYY-MM-DD'))
  }
  return dates
}

/**
 * 验证时间格式 HH:mm 或 HH:mm:ss
 */
export function isValidTimeFormat(timeStr: string): boolean {
  return /^(\d{1,2}):(\d{2})(:\d{2})?$/.test(timeStr)
}

/**
 * 验证日期格式 YYYY-MM-DD
 */
export function isValidDateFormat(dateStr: string): boolean {
  return dayjs(dateStr, 'YYYY-MM-DD', true).isValid()
}

/**
 * 分钟刻度转时间标签（如 720 -> "12:00"）
 */
export function minutesToTimeLabel(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}
