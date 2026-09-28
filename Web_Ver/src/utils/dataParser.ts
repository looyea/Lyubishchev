import * as XLSX from 'xlsx'
import type { TimeLogRecord, ImportResult, EventName, TimeCategory } from '@/types'
import { ALL_EVENTS, ALL_CATEGORIES } from '@/types'
import { calcDuration, isValidTimeFormat, getWeekNumber, getYear } from './timeUtils'

/**
 * 解析 Excel 文件，返回标准化的时间日志记录
 * 每个 Sheet 代表一个月的数据
 */
export async function parseExcelFile(file: File): Promise<{ records: TimeLogRecord[]; result: ImportResult }> {
  const buffer = await file.arrayBuffer()
  const workbook = XLSX.read(buffer, { type: 'array' })

  const records: TimeLogRecord[] = []
  const errors: string[] = []
  let totalRows = 0
  let successRows = 0

  for (const sheetName of workbook.SheetNames) {
    const sheet = workbook.Sheets[sheetName]
    const rows = XLSX.utils.sheet_to_json<Record<string, any>>(sheet)

    for (let i = 0; i < rows.length; i++) {
      totalRows++
      const row = rows[i]
      try {
        const record = transformRow(row, sheetName)
        if (record) {
          records.push(record)
          successRows++
        }
      } catch (e: any) {
        errors.push(`Sheet "${sheetName}" 第 ${i + 2} 行: ${e.message}`)
      }
    }
  }

  return {
    records,
    result: {
      totalRows,
      successRows,
      failedRows: totalRows - successRows,
      errors,
    },
  }
}

/**
 * 解析 CSV 文件
 */
export async function parseCSVFile(file: File): Promise<{ records: TimeLogRecord[]; result: ImportResult }> {
  const text = await file.text()
  const workbook = XLSX.read(text, { type: 'string' })
  const sheet = workbook.Sheets[workbook.SheetNames[0]]
  const rows = XLSX.utils.sheet_to_json<Record<string, any>>(sheet)

  const records: TimeLogRecord[] = []
  const errors: string[] = []
  let totalRows = rows.length
  let successRows = 0

  // CSV 中用日期字段推断月份
  for (let i = 0; i < rows.length; i++) {
    try {
      const record = transformRow(rows[i], '')
      if (record) {
        records.push(record)
        successRows++
      }
    } catch (e: any) {
      errors.push(`第 ${i + 2} 行: ${e.message}`)
    }
  }

  return {
    records,
    result: {
      totalRows,
      successRows,
      failedRows: totalRows - successRows,
      errors,
    },
  }
}

/**
 * 将原始行数据转换为标准记录
 */
function transformRow(row: Record<string, any>, _sheetName: string): TimeLogRecord | null {
  // 支持多种列名格式
  const date = normalizeDate(row['日期'] || row['date'] || row['Date'])
  const startTime = normalizeTime(row['开始时间'] || row['startTime'] || row['开始'])
  const endTime = normalizeTime(row['截止时间'] || row['结束时间'] || row['endTime'] || row['结束'])
  const category = normalizeCategory(row['时间分类'] || row['category'] || row['分类'])
  const event = normalizeEvent(row['事件'] || row['event'])
  const note = row['备注'] || row['note'] || ''

  if (!date || !startTime || !endTime || !category || !event) {
    return null
  }

  const duration = calcDuration(startTime, endTime)
  if (duration <= 0) return null

  return {
    date,
    startTime,
    endTime,
    duration,
    category: category as TimeCategory,
    event: event as EventName,
    weekNumber: getWeekNumber(date),
    year: getYear(date),
    note: note || undefined,
  }
}

function normalizeDate(val: any): string | null {
  if (!val) return null
  if (typeof val === 'number') {
    // Excel 日期序列号
    const d = XLSX.SSF.parse_date_code(val)
    if (d) {
      return `${d.y}-${String(d.m).padStart(2, '0')}-${String(d.d).padStart(2, '0')}`
    }
  }
  const str = String(val).trim()
  // 尝试 YYYY-MM-DD 或 YYYY/MM/DD
  const match = str.match(/(\d{4})[-/](\d{1,2})[-/](\d{1,2})/)
  if (match) {
    return `${match[1]}-${match[2].padStart(2, '0')}-${match[3].padStart(2, '0')}`
  }
  return null
}

function normalizeTime(val: any): string | null {
  if (!val) return null
  if (typeof val === 'number') {
    // Excel 时间序列号（小数部分）
    const totalMinutes = Math.round(val * 24 * 60)
    const h = Math.floor(totalMinutes / 60)
    const m = totalMinutes % 60
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }
  const str = String(val).trim()
  if (isValidTimeFormat(str)) {
    const parts = str.split(':')
    return `${parts[0].padStart(2, '0')}:${parts[1]}`
  }
  return null
}

function normalizeCategory(val: any): string | null {
  if (!val) return null
  const str = String(val).trim()
  if (ALL_CATEGORIES.includes(str as TimeCategory)) return str
  return null
}

function normalizeEvent(val: any): string | null {
  if (!val) return null
  const str = String(val).trim()
  if (ALL_EVENTS.includes(str as EventName)) return str
  return null
}
