package karika.distribucija.ba.salesrep.ui.analytics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import karika.distribucija.ba.salesrep.R

/** The bare light grid the web draws behind "Nema podataka za odabrani period" on an empty
 * "Trendovi prodaje" chart (same colour/thickness as the Vico chart guidelines). */
class AnalyticsEmptyGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.k_line)
        strokeWidth = resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        for (i in 0..ROWS) {
            val y = (h - paint.strokeWidth) * i / ROWS + paint.strokeWidth / 2
            canvas.drawLine(0f, y, w, y, paint)
        }
        for (i in 0..COLUMNS) {
            val x = (w - paint.strokeWidth) * i / COLUMNS + paint.strokeWidth / 2
            canvas.drawLine(x, 0f, x, h, paint)
        }
    }

    private companion object {
        const val ROWS = 4
        const val COLUMNS = 6
    }
}
