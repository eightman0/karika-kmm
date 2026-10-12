package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AnalyticsSeriesPoint
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaUiColors
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

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

/** "Nice" y-axis step (1/2/2.5/5 x 10^n) so that [ticks] steps cover [maxValue]. */
private fun niceStep(maxValue: Double, ticks: Int, integer: Boolean): Double {
    if (maxValue <= 0.0) return 1.0
    val raw = maxValue / ticks
    val magnitude = 10.0.pow(floor(log10(raw)))
    val normalized = raw / magnitude
    val nice = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 2.5 -> 2.5
        normalized <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude
    return if (integer) max(1.0, ceil(nice)) else nice
}

/**
 * Line chart for the "Trendovi prodaje" tab, matching the web: light horizontal grid with y-axis
 * labels, a smooth blue line with a dot per bucket, optional light fill under the line, and x
 * labels thinned out so they never overlap. With [hasData] false (or no points) it draws just the
 * empty grid with a centered "Nema podataka za odabrani period".
 */
@Composable
fun AnalyticsLineChart(
    points: List<AnalyticsSeriesPoint>,
    hasData: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().height(200.dp),
    showFill: Boolean = false,
    integerValues: Boolean = true,
    yLabel: (Double) -> String = { formatNumber(it) },
    lineColor: Color = KarikaColors.Blue,
    fillColor: Color = KarikaColors.Blue.copy(alpha = 0.12f),
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(color = KarikaUiColors.Muted, fontSize = 10.sp)
    val emptyStyle = TextStyle(color = KarikaUiColors.Muted, fontSize = 13.sp)
    val gridColor = KarikaUiColors.Line
    val empty = !hasData || points.isEmpty()

    Canvas(modifier = modifier) {
        val gap = 6.dp.toPx()
        val ticks = 4
        val maxValue = if (empty) 0.0 else points.maxOf { it.value }.toDouble()
        val step = niceStep(maxValue, ticks, integerValues)
        val tickCount = if (empty) ticks else max(1, ceil(maxValue / step).toInt()).coerceAtMost(ticks)
        val yMax = step * tickCount

        val yLabels = if (empty) emptyList() else (0..tickCount).map { textMeasurer.measure(yLabel(step * it), axisStyle) }
        val leftPad = (yLabels.maxOfOrNull { it.size.width } ?: 0).toFloat() + if (empty) 0f else gap
        val xLabelHeight = textMeasurer.measure("0", axisStyle).size.height.toFloat()
        val topPad = xLabelHeight / 2f
        val bottomPad = if (empty) topPad else xLabelHeight + gap
        val plotWidth = size.width - leftPad
        val plotHeight = size.height - topPad - bottomPad

        // Grid + y labels
        repeat(tickCount + 1) { i ->
            val y = topPad + plotHeight - plotHeight * i / tickCount
            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            yLabels.getOrNull(i)?.let { layout ->
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(leftPad - gap - layout.size.width, y - layout.size.height / 2f)
                )
            }
        }

        if (empty) {
            val layout = textMeasurer.measure("Nema podataka za odabrani period", emptyStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset((size.width - layout.size.width) / 2f, (size.height - layout.size.height) / 2f)
            )
            return@Canvas
        }

        val inset = 4.dp.toPx()
        val usableWidth = plotWidth - inset * 2
        val stepX = if (points.size > 1) usableWidth / (points.size - 1) else 0f
        val offsets = points.mapIndexed { index, point ->
            Offset(
                x = leftPad + inset + if (points.size > 1) index * stepX else usableWidth / 2f,
                y = topPad + plotHeight - (point.value / yMax.toFloat()) * plotHeight
            )
        }

        val linePath = smoothPath(offsets)
        if (showFill && offsets.size > 1) {
            val baseline = topPad + plotHeight
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(offsets.last().x, baseline)
                lineTo(offsets.first().x, baseline)
                close()
            }
            drawPath(path = fillPath, color = fillColor, style = Fill)
        }
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 2.dp.toPx()))
        offsets.forEach { offset ->
            drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = offset)
        }

        // X labels, skipping some so they never overlap
        val xLayouts = points.map { textMeasurer.measure(it.label, axisStyle) }
        val widest = (xLayouts.maxOfOrNull { it.size.width } ?: 0).toFloat() + gap
        val every = if (stepX <= 0f) 1 else max(1, ceil(widest / stepX).toInt())
        val labelY = topPad + plotHeight + gap
        xLayouts.forEachIndexed { index, layout ->
            if (index % every != 0) return@forEachIndexed
            val x = (offsets[index].x - layout.size.width / 2f)
                .coerceIn(leftPad, size.width - layout.size.width)
            drawText(textLayoutResult = layout, topLeft = Offset(x, labelY))
        }
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
