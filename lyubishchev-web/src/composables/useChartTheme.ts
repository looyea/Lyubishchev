import type { EChartsOption } from 'echarts'

// 与原项目 seaborn Accent 色系对应的配色方案
const ACCENT_COLORS = [
  '#7fc97f', '#beaed4', '#fdc086', '#ffff99',
  '#386cb0', '#f0027f', '#bf5b17', '#666666',
  '#a6cee3', '#1f78b4', '#b2df8a', '#33a02c',
]

/**
 * ECharts 统一主题配置
 */
export function useChartTheme() {
  const baseTheme: EChartsOption = {
    color: ACCENT_COLORS,
    textStyle: {
      fontFamily: '"Microsoft YaHei", "SimHei", sans-serif',
    },
    title: {
      textStyle: {
        fontFamily: '"Microsoft YaHei", "SimHei", sans-serif',
        fontSize: 16,
        fontWeight: 'bold',
      },
      left: 'center',
    },
    legend: {
      bottom: 0,
      textStyle: {
        fontFamily: '"Microsoft YaHei", "SimHei", sans-serif',
      },
    },
    tooltip: {
      trigger: 'item',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true,
    },
  }

  function getBarOption(title: string, categories: string[], seriesData: { name: string; data: number[] }[]): EChartsOption {
    return {
      ...baseTheme,
      title: { text: title },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { ...baseTheme.legend, data: seriesData.map(s => s.name) },
      xAxis: { type: 'category', data: categories },
      yAxis: { type: 'value', name: '分钟' },
      series: seriesData.map((s, i) => ({
        name: s.name,
        type: 'bar' as const,
        data: s.data,
        itemStyle: { color: ACCENT_COLORS[i % ACCENT_COLORS.length] },
        label: { show: true, position: 'top' as const },
      })),
    }
  }

  function getRadarOption(title: string, indicators: { name: string; max: number }[], seriesData: { name: string; values: number[] }[]): EChartsOption {
    return {
      ...baseTheme,
      title: { text: title },
      tooltip: { trigger: 'item' },
      legend: { ...baseTheme.legend, data: seriesData.map(s => s.name) },
      radar: {
        indicator: indicators,
        shape: 'polygon',
        splitNumber: 5,
        axisName: {
          color: '#333',
          fontFamily: '"Microsoft YaHei", "SimHei", sans-serif',
        },
        splitLine: { lineStyle: { type: 'dashed' } },
      },
      series: [{
        type: 'radar',
        data: seriesData.map((s, i) => ({
          name: s.name,
          value: s.values,
          areaStyle: { opacity: 0.3 },
          lineStyle: { width: 2 },
          itemStyle: { color: ACCENT_COLORS[i % ACCENT_COLORS.length] },
        })),
      }],
    }
  }

  function getLineOption(title: string, xData: string[], seriesData: { name: string; data: number[] }[]): EChartsOption {
    return {
      ...baseTheme,
      title: { text: title },
      tooltip: { trigger: 'axis' },
      legend: { ...baseTheme.legend, data: seriesData.map(s => s.name) },
      xAxis: {
        type: 'category',
        data: xData,
        axisLabel: {
          interval: Math.floor(xData.length / 12),
          rotate: 30,
        },
      },
      yAxis: { type: 'value', name: '分钟计数' },
      series: seriesData.map((s, i) => ({
        name: s.name,
        type: 'line' as const,
        data: s.data,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 1.5 },
        itemStyle: { color: ACCENT_COLORS[i % ACCENT_COLORS.length] },
      })),
    }
  }

  function getPieOption(title: string, data: { name: string; value: number }[]): EChartsOption {
    return {
      ...baseTheme,
      title: { text: title },
      tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
      legend: { ...baseTheme.legend, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '50%'],
        data: data.map((d, i) => ({
          ...d,
          itemStyle: { color: ACCENT_COLORS[i % ACCENT_COLORS.length] },
        })),
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
        label: { formatter: '{b}\n{d}%' },
      }],
    }
  }

  return {
    baseTheme,
    ACCENT_COLORS,
    getBarOption,
    getRadarOption,
    getLineOption,
    getPieOption,
  }
}
