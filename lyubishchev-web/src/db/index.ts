import Dexie, { type Table } from 'dexie'
import type { TimeLogRecord, WeekSettlementRecord } from '@/types'

export class LyubishchevDB extends Dexie {
  timeLogs!: Table<TimeLogRecord, number>
  weekReports!: Table<WeekSettlementRecord, number>

  constructor() {
    super('LyubishchevDB')
    this.version(1).stores({
      timeLogs: '++id, date, category, event, weekNumber, year',
    })
    this.version(2).stores({
      weekReports: '++id, [year+weekNumber], weekStart',
    })
  }
}

export const db = new LyubishchevDB()
