package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AnalyticsSeriesPoint
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText

private fun smoothPath(offsets: List<Offset>): Path {
    val path = Path()
    if (offsets.isEmpty()) return path
    path.moveTo(offsets.first().x, offsets.first().y)
    for (i in 0 until offsets.size - 1) {
        val p0 = offsets[i]
        val p1 = offsets[i + 1]
        val dx = (p1.x - p0.x) / 2f
        path.cubicTo(p0.x + dx, p0.y, p1.x - dx, p1.y, p1.x, p1.y)
    }
    return path
}

@Composable
private fun AxisLabels(points: List<AnalyticsSeriesPoint>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val visibleLabels = if (points.size <= 6) {
            points.map { it.label }
        } else {
            val step = (points.size - 1) / 4
            (0..4).map { points[(it * step).coerceAtMost(points.size - 1)].label }
        }
        visibleLabels.forEach { label ->
            KarikaText(
                modifier = Modifier.weight(1f),
                text = label,
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnalyticsAreaChart(
    points: List<AnalyticsSeriesPoint>,
    modifier: Modifier = Modifier.fillMaxWidth().height(130.dp),
    lineColor: Color = KarikaColors.Blue,
    fillColor: Color = KarikaColors.Blue.copy(alpha = 0.12f),
    showAxisLabels: Boolean = true,
) {
    if (points.isEmpty()) return
    val maxValue = (points.maxOf { it.value }).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f

        val offsets = points.mapIndexed { index, point ->
            Offset(
                x = index * stepX,
                y = chartHeight - (point.value / maxValue) * chartHeight
            )
        }

        if (showAxisLabels) {
            val gridLines = 4
            repeat(gridLines + 1) { i ->
                val y = chartHeight * i / gridLines
                drawLine(
                    color = KarikaColors.Border.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        val linePath = smoothPath(offsets)
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(offsets.last().x, chartHeight)
            lineTo(offsets.first().x, chartHeight)
            close()
        }
        drawPath(path = fillPath, color = fillColor, style = Fill)
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 2.5.dp.toPx()))

        offsets.forEach { offset ->
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = offset)
        }
    }

    if (showAxisLabels) {
        AxisLabels(points = points)
    }
}

@Composable
fun AnalyticsStepChart(
    points: List<AnalyticsSeriesPoint>,
    modifier: Modifier = Modifier.fillMaxWidth().height(70.dp),
    lineColor: Color = KarikaColors.Blue,
    showAxisLabels: Boolean = true,
) {
    if (points.isEmpty()) return
    val maxValue = (points.maxOf { it.value }).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f

        val offsets = points.mapIndexed { index, point ->
            Offset(
                x = index * stepX,
                y = chartHeight - (point.value / maxValue) * chartHeight
            )
        }

        val path = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            for (i in 1 until offsets.size) {
                val prev = offsets[i - 1]
                val curr = offsets[i]
                val midX = (prev.x + curr.x) / 2f
                lineTo(midX, prev.y)
                lineTo(midX, curr.y)
                lineTo(curr.x, curr.y)
            }
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
    }

    if (showAxisLabels) {
        AxisLabels(points = points)
    }
}

@Composable
fun AnalyticsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier.fillMaxWidth().height(8.dp),
    trackColor: Color = KarikaColors.Gray9,
    progressColor: Color = KarikaColors.Blue,
) {
    Canvas(modifier = modifier) {
        val radius = size.height / 2f
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(radius, radius)
        )
        val clamped = progress.coerceIn(0f, 1f)
        if (clamped > 0f) {
            drawRoundRect(
                color = progressColor,
                size = size.copy(width = size.width * clamped),
                cornerRadius = CornerRadius(radius, radius)
            )
        }
    }
}
