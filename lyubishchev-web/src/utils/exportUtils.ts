import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

/**
 * 将指定 DOM 元素导出为 PNG 图片
 */
export async function exportToPNG(elementId: string, fileName: string = 'chart') {
  const el = document.getElementById(elementId)
  if (!el) throw new Error(`Element #${elementId} not found`)

  const canvas = await html2canvas(el, {
    backgroundColor: '#ffffff',
    scale: 2,
  })

  const link = document.createElement('a')
  link.download = `${fileName}.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
}

/**
 * 将指定 DOM 元素导出为 PDF
 */
export async function exportToPDF(elementId: string, fileName: string = 'report') {
  const el = document.getElementById(elementId)
  if (!el) throw new Error(`Element #${elementId} not found`)

  const canvas = await html2canvas(el, {
    backgroundColor: '#ffffff',
    scale: 2,
  })

  const imgData = canvas.toDataURL('image/png')
  const imgWidth = canvas.width
  const imgHeight = canvas.height

  // A4 尺寸
  const pdfWidth = 210
  const pdfHeight = (imgHeight * pdfWidth) / imgWidth

  const pdf = new jsPDF({
    orientation: pdfHeight > 297 ? 'l' : 'p',
    unit: 'mm',
    format: 'a4',
  })

  const finalWidth = pdfWidth
  const finalHeight = (imgHeight * pdfWidth) / imgWidth

  pdf.addImage(imgData, 'PNG', 0, 0, finalWidth, finalHeight)
  pdf.save(`${fileName}.pdf`)
}

/**
 * 从 ECharts 实例直接导出图片
 */
export function exportChartImage(chartInstance: any, fileName: string = 'chart') {
  if (!chartInstance) return
  const url = chartInstance.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff',
  })
  const link = document.createElement('a')
  link.download = `${fileName}.png`
  link.href = url
  link.click()
}
