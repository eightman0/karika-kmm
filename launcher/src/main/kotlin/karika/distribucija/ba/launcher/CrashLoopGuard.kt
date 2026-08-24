package karika.distribucija.ba.launcher

import android.annotation.SuppressLint
import android.content.Context

/**
 * Backstop for the crash-recovery relaunch in LauncherApp: an unattended kiosk device that
 * crashes on startup (e.g. corrupted persisted state) would otherwise relaunch into the same
 * crash forever. Once too many crashes land within the window, escalate to a full device reboot
 * (see [shouldReboot]) rather than just letting the process die - a kiosk with nothing running
 * and no way for whoever's in front of it to exit lock task is worse than one that keeps trying.
 */
object CrashLoopGuard {
    private const val PREFS = "crash_loop_guard"
    private const val KEY_TIMESTAMPS = "timestamps"
    private const val KEY_LAST_REBOOT = "last_reboot_at"
    private const val MAX_CRASHES_IN_WINDOW = 3
    private const val WINDOW_MS = 60_000L
    private const val MIN_REBOOT_INTERVAL_MS = 5 * 60_000L

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

    /** Called once in-process relaunching has been exhausted. Throttled independently of
     * [shouldRelaunch] (a much longer window) so a truly unrecoverable crash doesn't reboot the
     * device in a tight loop - it still keeps trying, just slowly enough to not be destructive. */
    @SuppressLint("ApplySharedPref")
    fun shouldReboot(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastReboot = prefs.getLong(KEY_LAST_REBOOT, 0L)
        if (now - lastReboot < MIN_REBOOT_INTERVAL_MS) return false

        prefs.edit().putLong(KEY_LAST_REBOOT, now).commit()
        return true
    }
}
