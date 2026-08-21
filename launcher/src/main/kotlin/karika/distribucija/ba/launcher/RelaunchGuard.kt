package karika.distribucija.ba.launcher

import android.annotation.SuppressLint
import android.content.Context

/**
 * Stops the launcher from bouncing forever if a payload app crashes on every launch: once too
 * many auto-relaunch attempts land within the window, stop auto-launching and just leave its
 * tile visible for a manual tap - so a human notices, instead of an endless flicker.
 */
object RelaunchGuard {
    private const val PREFS = "relaunch_guard"
    private const val MAX_ATTEMPTS_IN_WINDOW = 3
    private const val WINDOW_MS = 60_000L

    @SuppressLint("ApplySharedPref")
    fun shouldAutoLaunch(context: Context, packageName: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "timestamps_$packageName"
        val now = System.currentTimeMillis()
        val recentAttempts = prefs.getString(key, "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it < WINDOW_MS }

        val updatedAttempts = recentAttempts + now
        prefs.edit()
            .putString(key, updatedAttempts.joinToString(","))
            .commit()

        return updatedAttempts.size <= MAX_ATTEMPTS_IN_WINDOW
    }
}
