package karika.distribucija.ba.salesrep.util

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

/**
 * Minimal pinch-to-zoom + drag-to-pan ImageView (no Coil/zoomable-image dependency available in
 * :salesrep). Mirrors composeApp's full-screen ImagePreview.kt (a `ZoomableImage` from Coil)
 * closely enough for message-attachment previews: starts fit-centered, pinch scales 1x-5x, and
 * dragging pans while zoomed in. Dismissal is via a dedicated close button in the hosting
 * fragment, matching the Compose preview's explicit close icon rather than a tap-to-dismiss.
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val baseMatrix = Matrix()
    private val drawMatrix = Matrix()
    private var scale = 1f
    private val minScale = 1f
    private val maxScale = 5f
    private val lastPoint = PointF()
    private var isDragging = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val previousScale = scale
            scale = (scale * detector.scaleFactor).coerceIn(minScale, maxScale)
            drawMatrix.postScale(scale / previousScale, scale / previousScale, detector.focusX, detector.focusY)
            imageMatrix = drawMatrix
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeBaseMatrix()
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        computeBaseMatrix()
    }

    private fun computeBaseMatrix() {
        val d = drawable ?: return
        if (width == 0 || height == 0 || d.intrinsicWidth <= 0 || d.intrinsicHeight <= 0) return

        val fitScale = minOf(width.toFloat() / d.intrinsicWidth, height.toFloat() / d.intrinsicHeight)
        baseMatrix.reset()
        baseMatrix.postScale(fitScale, fitScale)
        baseMatrix.postTranslate(
            (width - d.intrinsicWidth * fitScale) / 2f,
            (height - d.intrinsicHeight * fitScale) / 2f
        )
        drawMatrix.set(baseMatrix)
        scale = 1f
        imageMatrix = drawMatrix
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastPoint.set(event.x, event.y)
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && scale > minScale && event.pointerCount == 1) {
                    drawMatrix.postTranslate(event.x - lastPoint.x, event.y - lastPoint.y)
                    imageMatrix = drawMatrix
                    lastPoint.set(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDragging = false
        }
        return true
    }
}
