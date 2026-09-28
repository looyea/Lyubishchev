import { ref, computed, watch } from 'vue'
import dayjs from 'dayjs'
import isoWeek from 'dayjs/plugin/isoWeek'
import { useTimeLogStore } from '@/stores/timeLog'
import { useSettlementStore } from '@/stores/settlement'
import { aggregateByWeek } from '@/utils/dataAggregator'
import { weekDateList, weekDayLabels } from '@/utils/timeUtils'
import { ALL_CATEGORIES, ALL_EVENTS, L1_EVENTS, L2_EVENTS } from '@/types'
import type { WeekAggregation, WeekSettlementRecord } from '@/types'

dayjs.extend(isoWeek)

/**
 * 周报核心逻辑：以"周"为单位组织数据，本周为主、穿插上周对比
 */
export function useWeekReport() {
  const store = useTimeLogStore()
  const settlementStore = useSettlementStore()

  // 锚点日期：决定"当前查看的是哪一周"
  const anchorDate = ref(dayjs().format('YYYY-MM-DD'))

  const weekRange = computed(() => {
    const d = dayjs(anchorDate.value)
    return {
      start: d.startOf('isoWeek').format('YYYY-MM-DD'),
      end: d.endOf('isoWeek').format('YYYY-MM-DD'),
      year: d.isoWeekYear(),
      weekNumber: d.isoWeek(),
    }
  })

  const prevWeekRange = computed(() => {
    const d = dayjs(anchorDate.value).subtract(1, 'week')
    return {
      start: d.startOf('isoWeek').format('YYYY-MM-DD'),
      end: d.endOf('isoWeek').format('YYYY-MM-DD'),
    }
  })

  const dayLabels = computed(() => weekDayLabels(weekRange.value.start))
  const currentDays = computed(() => weekDateList(weekRange.value.start))
  const previousDays = computed(() => weekDateList(prevWeekRange.value.start))

  const emptyAgg = (): WeekAggregation => ({
    categoryTotals: {},
    eventTotals: {},
    dailyTotals: {},
    totalMinutes: 0,
  })

  const current = computed<WeekAggregation>(() => {
    if (store.records.length === 0) return emptyAgg()
    return aggregateByWeek(store.records, weekRange.value.start, weekRange.value.end)
  })

  const previous = computed<WeekAggregation>(() => {
    if (store.records.length === 0) return emptyAgg()
    return aggregateByWeek(store.records, prevWeekRange.value.start, prevWeekRange.value.end)
  })

  const hasAnyRecord = computed(() => current.value.totalMinutes > 0)

  /** 按维度构造 本周/上周 数值数组 */
  function seriesFor(items: string[]) {
    return {
      current: items.map(i => current.value.eventTotals[i] ?? current.value.categoryTotals[i] ?? 0),
      previous: items.map(i => previous.value.eventTotals[i] ?? previous.value.categoryTotals[i] ?? 0),
    }
  }

  const categorySeries = computed(() => seriesFor([...ALL_CATEGORIES]))
  const l1Series = computed(() => seriesFor([...L1_EVENTS]))
  const l2Series = computed(() => seriesFor([...L2_EVENTS]))
  const eventSeries = computed(() => seriesFor([...ALL_EVENTS]))

  /** 逐日对比（按周一~周日对齐） */
  const dailySeries = computed(() => ({
    current: currentDays.value.map(d => current.value.dailyTotals[d] || 0),
    previous: previousDays.value.map(d => previous.value.dailyTotals[d] || 0),
  }))

  /** 记录完整度：本周有几天留下了记录 */
  const activeDayCount = computed(() =>
    currentDays.value.filter(d => (current.value.dailyTotals[d] || 0) > 0).length,
  )

  /** 日均用时（按有记录的天数计） */
  const dailyAverage = computed(() =>
    activeDayCount.value === 0 ? 0 : Math.round(current.value.totalMinutes / activeDayCount.value),
  )

  const totalChangeRate = computed(() => {
    const prev = previous.value.totalMinutes
    const curr = current.value.totalMinutes
    if (prev === 0) return curr > 0 ? 100 : 0
    return Math.round(((curr - prev) / prev) * 100)
  })

  /** 明细表：事件维度，含上周对照与环比 */
  const eventTable = computed(() =>
    ALL_EVENTS.map(evt => {
      const curr = current.value.eventTotals[evt] || 0
      const prev = previous.value.eventTotals[evt] || 0
      return {
        event: evt,
        category: L1_EVENTS.includes(evt as any) ? 'I类时间' : 'II类时间',
        current: curr,
        previous: prev,
        diff: curr - prev,
        rate: prev === 0 ? (curr > 0 ? 100 : 0) : Math.round(((curr - prev) / prev) * 100),
      }
    }),
  )

  /** 当前查看周对应的结存记录 */
  const settlement = ref<WeekSettlementRecord | undefined>(undefined)

  async function refreshSettlement() {
    settlement.value = await settlementStore.findByWeek(
      weekRange.value.year,
      weekRange.value.weekNumber,
    )
  }

  watch([weekRange, () => store.records.length], refreshSettlement, { immediate: true })

  function goPrevWeek() {
    anchorDate.value = dayjs(anchorDate.value).subtract(1, 'week').format('YYYY-MM-DD')
  }

  function goNextWeek() {
    anchorDate.value = dayjs(anchorDate.value).add(1, 'week').format('YYYY-MM-DD')
  }

  function goThisWeek() {
    anchorDate.value = dayjs().format('YYYY-MM-DD')
  }

  async function settleWeek(note?: string) {
    const { year, weekNumber, start, end } = weekRange.value
    await settlementStore.settle({
      year,
      weekNumber,
      weekStart: start,
      weekEnd: end,
      summary: current.value,
      note,
    })
    await refreshSettlement()
  }

  return {
    anchorDate,
    weekRange,
    prevWeekRange,
    dayLabels,
    current,
    previous,
    hasAnyRecord,
    categorySeries,
    l1Series,
    l2Series,
    eventSeries,
    dailySeries,
    activeDayCount,
    dailyAverage,
    totalChangeRate,
    eventTable,
    settlement,
    settlements: computed(() => settlementStore.settlements),
    goPrevWeek,
    goNextWeek,
    goThisWeek,
    settleWeek,
    loadSettlements: settlementStore.loadAll,
  }
}
