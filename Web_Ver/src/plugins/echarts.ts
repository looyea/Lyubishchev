/**
 * ECharts 按需引入注册
 *
 * vue-echarts v7 不自带任何图表与渲染器，必须先 use([...]) 注册，
 * 否则运行时会报 "Renderer 'undefined' is not imported"。
 * 新增图表类型时，记得在此处补充对应的 Chart 与 Component。
 */
import { use, init, registerTheme } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart, RadarChart } from 'echarts/charts'
import {
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkAreaComponent,
} from 'echarts/components'
import { LabelLayout } from 'echarts/features'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  RadarChart,
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkAreaComponent,
  LabelLayout,
])

// 与原项目 seaborn 风格一致的浅色主题，供 option 中 theme="lyubishchev" 使用
registerTheme('lyubishchev', {
  color: [
    '#7fc97f', '#beaed4', '#fdc086', '#ffff99',
    '#386cb0', '#f0027f', '#bf5b17', '#666666',
  ],
})

export { use, init, registerTheme }
