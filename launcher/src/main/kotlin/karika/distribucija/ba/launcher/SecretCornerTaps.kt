package karika.distribucija.ba.launcher

import android.os.SystemClock
import android.view.MotionEvent

/** Hidden service gesture: [COUNT] taps in the top-left corner within [WINDOW_MS]. The kiosk
 * payload app counts the same gesture and opens ServiceUnlockActivity the same way. */
class SecretCornerTaps(private val cornerPx: Float, private val onTriggered: () -> Unit) {
    private var count = 0
    private var firstTapAt = 0L

    fun onTouch(event: MotionEvent) {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return
        if (event.x > cornerPx || event.y > cornerPx) {
            count = 0
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (count == 0 || now - firstTapAt > WINDOW_MS) {
            count = 0
            firstTapAt = now
        }
        count++
        if (count >= COUNT) {
            count = 0
            onTriggered()
        }
    }

    private companion object {
        const val COUNT = 7
        const val WINDOW_MS = 4_000L
    }
}
