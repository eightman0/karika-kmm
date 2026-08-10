package karika.distribucija.ba.salesrep.util

import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads this view's bottom edge by however much the on-screen keyboard currently overlaps it, so
 * content anchored to the bottom (e.g. a message input bar sitting below a weighted
 * RecyclerView) rises to clear the keyboard when it opens. MainActivity draws edge-to-edge
 * (`decorFitsSystemWindows(false)`), so `windowSoftInputMode="adjustResize"` alone no longer
 * resizes fragment content automatically - this replicates that behavior manually, mirroring
 * composeApp's `Modifier.imePadding()` on the same screens' input bars.
 *
 * Also re-scrolls the currently focused descendant into view once the padding lands: a field can
 * request its own visibility on focus, but that happens before the keyboard has actually resized
 * anything, so it doesn't yet know how much of the bottom the keyboard will cover - without this,
 * a field further down a scrollable form (e.g. Order Review's note) can end up focused but hidden
 * behind the keyboard until the user manually scrolls.
 */
fun View.applyImeBottomPadding() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(bottom = initialBottom + imeBottom)
        if (imeBottom > 0) {
            view.post {
                view.findFocus()?.let { focused ->
                    focused.requestRectangleOnScreen(Rect(0, 0, focused.width, focused.height), true)
                }
            }
        }
        insets
    }
}
