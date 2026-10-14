package karika.distribucija.ba.salesrep.util

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import karika.distribucija.ba.salesrep.BuildConfig

/**
 * Diagonal environment ribbon in the top-right corner on every non-prod build ("stage", "demo") -
 * mirrors WaterMarkBox in composeApp's AppView.kt (same corner, angle and per-env colors). Added
 * to the window's decor view so it sits above the toolbar and drawer, and never takes touches.
 */
object EnvWatermark {
    fun attach(activity: Activity) {
        val env = BuildConfig.ENV_NAME
        if (env == "prod") return
        val decor = activity.window.decorView as? FrameLayout ?: return
        val density = activity.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val ribbon = TextView(activity).apply {
            text = env
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundColor(
                when (env) {
                    "demo" -> Color.parseColor("#FFC107")
                    "stage" -> Color.parseColor("#3397C1")
                    else -> Color.BLACK
                }
            )
            setTextColor(if (env == "demo") Color.parseColor("#242424") else Color.WHITE)
            rotation = 45f
            translationX = dp(64).toFloat()
            translationY = dp(50).toFloat()
            isClickable = false
            isFocusable = false
            importantForAccessibility = TextView.IMPORTANT_FOR_ACCESSIBILITY_NO
            elevation = 100 * density
        }
        decor.addView(ribbon, FrameLayout.LayoutParams(dp(300), dp(40), Gravity.TOP or Gravity.END))
    }
}
