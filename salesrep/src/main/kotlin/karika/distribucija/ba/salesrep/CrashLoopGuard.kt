package karika.distribucija.ba.salesrep

import android.annotation.SuppressLint
import android.content.Context

/**
 * Backstop for the crash-recovery relaunch in SalesRepApp: an unattended kiosk device that
 * crashes on startup (e.g. corrupted persisted state) would otherwise relaunch into the same
 * crash forever. Once too many crashes land within the window, stop relaunching and let the
 * process just die - Android's own lock-task recovery and the next WorkManager/boot trigger are
 * still there, but this stops us from spinning the device in a tight loop.
 */
object CrashLoopGuard {
    private const val PREFS = "crash_loop_guard"
    private const val KEY_TIMESTAMPS = "timestamps"
    private const val MAX_CRASHES_IN_WINDOW = 3
    private const val WINDOW_MS = 60_000L

    @SuppressLint("ApplySharedPref")
    fun shouldRelaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val recentCrashes = prefs.getString(KEY_TIMESTAMPS, "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it < WINDOW_MS }

        val updatedCrashes = recentCrashes + now
        prefs.edit()
            .putString(KEY_TIMESTAMPS, updatedCrashes.joinToString(","))
            .commit()

        return updatedCrashes.size <= MAX_CRASHES_IN_WINDOW
    }
}
