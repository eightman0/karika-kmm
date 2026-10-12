package karika.distribucija.ba.salesrep.util

import android.app.Activity
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import karika.distribucija.ba.salesrep.R

/**
 * In-app message at the top of the screen - mirrors KSnackbar in composeApp's KarikaScaffold.kt
 * (accent card, red for errors, type icon, close mark, tap to dismiss, gone after 1.5 s). Replaces
 * Toast, which Android 12+ always draws at the bottom with the app icon attached.
 */
object KSnackbar {
    enum class Type { SUCCESS, WARNING, ERROR }

    private const val TAG = "k_snackbar"
    private const val DURATION_MS = 1500L
    private const val ANIM_MS = 180L

    fun show(activity: Activity, message: CharSequence?, type: Type = Type.ERROR) {
        if (message.isNullOrBlank()) return
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        root.findViewWithTag<View>(TAG)?.let { root.removeView(it) }

        val view = LayoutInflater.from(activity).inflate(R.layout.view_k_snackbar, root, false)
        view.tag = TAG
        view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(activity, if (type == Type.ERROR) R.color.k_red else R.color.k_accent)
        )
        view.findViewById<ImageView>(R.id.k_snackbar_icon).setImageResource(
            when (type) {
                Type.ERROR -> R.drawable.ic_k_snack_close
                Type.WARNING -> R.drawable.ic_k_snack_info
                Type.SUCCESS -> R.drawable.ic_k_snack_check
            }
        )
        view.findViewById<TextView>(R.id.k_snackbar_message).text = message

        val statusBar = ViewCompat.getRootWindowInsets(root)
            ?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        )
        val margin = (activity.resources.displayMetrics.density * 16).toInt()
        val marginVertical = (activity.resources.displayMetrics.density * 8).toInt()
        params.setMargins(margin, statusBar + marginVertical, margin, marginVertical)
        root.addView(view, params)

        val dismiss = Runnable { dismiss(root, view) }
        view.setOnClickListener {
            view.removeCallbacks(dismiss)
            dismiss(root, view)
        }
        view.alpha = 0f
        view.post {
            view.translationY = -view.height.toFloat() / 2
            view.animate().alpha(1f).translationY(0f).setDuration(ANIM_MS).start()
        }
        view.postDelayed(dismiss, DURATION_MS)
    }

    private fun dismiss(root: ViewGroup, view: View) {
        if (view.parent == null) return
        view.animate().alpha(0f).translationY(-view.height.toFloat() / 2).setDuration(ANIM_MS)
            .withEndAction { root.removeView(view) }
            .start()
    }
}

fun Fragment.showSnack(message: CharSequence?, type: KSnackbar.Type = KSnackbar.Type.ERROR) {
    activity?.let { KSnackbar.show(it, message, type) }
}

fun Fragment.showSnack(@StringRes message: Int, type: KSnackbar.Type = KSnackbar.Type.ERROR) {
    activity?.let { KSnackbar.show(it, it.getString(message), type) }
}
