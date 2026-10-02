package com.lyubishchev.timekeeper.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyubishchev.timekeeper.domain.TimeRules
import kotlin.math.cos
import kotlin.math.sin

private const val RINGS = 4

/**
 * 对比期取色：主色往卡片底色退一档，浅色模式变浅、深色模式变暗，两个方向都是"往背景靠"。
 * The previous-period color: the primary hue stepped toward the card surface — lighter on
 * light backgrounds, darker on dark ones — so the two series never rely on hue distance again.
 */
fun radarPreviousColor(scheme: ColorScheme): Color =
    lerp(scheme.primary, scheme.surface, 0.45f)

/** 对比期虚线的疏密参数（6dp 实段 / 4dp 空段），图表描边与概览页图例共用 */
fun Density.radarDashIntervals(): FloatArray =
    floatArrayOf(6.dp.toPx(), 4.dp.toPx())

/**
 * 手写雷达图：两组数据叠在同一组轴上，统一按 [scaleMax] 归一。
 * A hand-drawn radar: two series over one axis set, both normalised to the same scale.
 *
 * 不引第三方图表库——雷达图在原生库里支持度不稳，自己画更可控。
 *
 * @param axes 轴名（一类时间 6 轴 / 二类时间 3 轴）
 * @param current 当期数值，与 axes 等长，实心高亮
 * @param previous 对比期数值，淡填充底图
 */
@Composable
fun RadarChart(
    axes: List<String>,
    current: List<Int>,
    previous: List<Int>,
    scaleMax: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val currentColor = scheme.primary
    val previousColor = radarPreviousColor(scheme)
    val gridColor = scheme.outline
    val labelColor = scheme.onSurfaceVariant
    val measurer = rememberTextMeasurer()

    Canvas(modifier) {
        val count = axes.size
        if (count < 3 || current.size < count || previous.size < count || scaleMax <= 0) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        val labelRoom = 46.dp.toPx()
        val radius = minOf(size.width, size.height) / 2f - labelRoom
        if (radius <= 16f) return@Canvas

        drawGrid(center, radius, count, gridColor)
        drawSeries(center, radius, scaleMax, previous, previousColor, fillAlpha = 0.12f, dots = false, dashed = true)
        drawSeries(center, radius, scaleMax, current, currentColor, fillAlpha = 0.28f, dots = true)

        axes.forEachIndexed { index, name ->
            val angle = angleOf(index, count)
            val ux = cos(angle)
            val uy = sin(angle)
            val anchor = Offset(
                center.x + (radius + 12.dp.toPx()) * ux,
                center.y + (radius + 12.dp.toPx()) * uy,
            )
            val minutes = current.getOrElse(index) { 0 }
            val label = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 12.sp, color = labelColor)) { append(name) }
                withStyle(SpanStyle(fontSize = 11.sp, color = currentColor, fontWeight = FontWeight.Medium)) {
                    append("\n")
                    append(TimeRules.formatHours(minutes))
                    append("h")
                }
            }
            drawAnchoredText(measurer, label, anchor, ux, uy)
        }
    }
}

private fun angleOf(index: Int, count: Int): Float =
    Math.toRadians(-90.0 + 360.0 * index / count).toFloat()

private fun DrawScope.drawGrid(center: Offset, radius: Float, count: Int, color: Color) {
    val grid = Path()
    for (ring in 1..RINGS) {
        val rr = radius * ring / RINGS
        for (i in 0 until count) {
            val a0 = angleOf(i, count)
            val a1 = angleOf((i + 1) % count, count)
            grid.moveTo(center.x + rr * cos(a0), center.y + rr * sin(a0))
            grid.lineTo(center.x + rr * cos(a1), center.y + rr * sin(a1))
        }
    }
    for (i in 0 until count) {
        val a = angleOf(i, count)
        grid.moveTo(center.x, center.y)
        grid.lineTo(center.x + radius * cos(a), center.y + radius * sin(a))
    }
    drawPath(grid, color, style = Stroke(width = 1.dp.toPx()))
}

private fun DrawScope.drawSeries(
    center: Offset,
    radius: Float,
    scaleMax: Int,
    values: List<Int>,
    color: Color,
    fillAlpha: Float,
    dots: Boolean,
    dashed: Boolean = false,
) {
    if (values.size < 3) return
    val points = ArrayList<Offset>(values.size)
    values.forEachIndexed { index, minutes ->
        val ratio = (minutes.coerceAtMost(scaleMax) / scaleMax.toFloat()).coerceIn(0f, 1f)
        val angle = angleOf(index, values.size)
        points += Offset(
            center.x + radius * ratio * cos(angle),
            center.y + radius * ratio * sin(angle),
        )
    }
    val shape = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    // pathEffect 只给描边用；填充 Path 不支持虚线
    val stroke = if (dashed) {
        Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(radarDashIntervals()))
    } else {
        Stroke(width = 2.dp.toPx())
    }
    drawPath(shape, color, alpha = fillAlpha)
    drawPath(shape, color, style = stroke)
    if (dots) points.forEach { drawCircle(color = color, radius = 3.dp.toPx(), center = it) }
}

/** 按轴方向把文字块贴到顶点外侧：顶部轴居上、底部轴居下、左右各自外扩。 */
private fun DrawScope.drawAnchoredText(
    measurer: TextMeasurer,
    text: AnnotatedString,
    at: Offset,
    ux: Float,
    uy: Float,
) {
    val block = measurer.measure(text, style = TextStyle.Default).size
    val width = block.width.toFloat()
    val height = block.height.toFloat()
    val dx = when {
        ux > 0.3f -> 0f
        ux < -0.3f -> -width
        else -> -width / 2f
    }
    val dy = when {
        uy > 0.3f -> 0f
        uy < -0.3f -> -height
        else -> -height / 2f
    }
    drawText(measurer, text, topLeft = Offset(at.x + dx, at.y + dy))
}
