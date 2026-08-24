package karika.distribucija.ba.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * Set by UpdateWorker while it's downloading/installing, read by LauncherActivity to show the
 * maintenance screen instead of trying to auto-launch the payload app mid-replacement. Persisted
 * (not just in-memory) since WorkManager and the Activity can run across separate process starts.
 * Auto-expires after MAX_AGE_MS so a worker that died without clearing it can't strand the device
 * on the maintenance screen forever.
 */
object MaintenanceState {
    private const val PREFS = "maintenance_state"
    private const val KEY_SINCE = "maintenance_since"
    private const val MAX_AGE_MS = 20 * 60 * 1000L

    fun begin(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_SINCE, System.currentTimeMillis())
            .apply()
    }

    fun end(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_SINCE)
            .apply()
    }

    fun isActive(context: Context): Boolean {
        val since = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_SINCE, 0L)
        if (since == 0L) return false
        return System.currentTimeMillis() - since < MAX_AGE_MS
    }

    /** Lets a visible LauncherActivity react the moment begin()/end() writes, instead of only
     * re-checking isActive() on its next onResume (which may not come until something else, e.g.
     * a screen wake, happens to trigger one). */
    fun addChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(listener)
    }
}
