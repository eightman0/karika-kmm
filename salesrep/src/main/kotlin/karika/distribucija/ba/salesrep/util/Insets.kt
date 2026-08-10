package karika.distribucija.ba.salesrep.util

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
 */
fun View.applyImeBottomPadding() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(bottom = initialBottom + imeBottom)
        insets
    }
}
