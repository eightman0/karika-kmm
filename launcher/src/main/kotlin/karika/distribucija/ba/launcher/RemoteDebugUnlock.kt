package karika.distribucija.ba.launcher

import android.content.Context
import karika.distribucija.ba.launcher.update.UpdateScheduler

/**
 * Temporary, admin-triggered flag that tells LauncherKiosk to skip re-engaging lock task on
 * resume - lets a technician plug in ADB and get through the one-time authorization dialog, which
 * some OEM builds refuse to draw over a pinned lock-task activity (observed on a real device:
 * "unauthorized" forever, no dialog ever showing, until lock task was out of the way).
 *
 * Time-bound rather than a plain on/off switch: an unpinned kiosk is not actually locked down -
 * whoever is in front of it could leave the app, open Settings freely, uninstall things, etc. for
 * as long as this stays active, so it auto-expires even if nobody remembers to turn it back off
 * (see UpdateScheduler.scheduleAutoRelock, which forces the check regardless of whether anything
 * else would have resumed the launcher by then).
 */
object RemoteDebugUnlock {
    private const val PREFS = "remote_debug_unlock"
    private const val KEY_EXPIRES_AT = "expires_at_millis"
    const val DURATION_MINUTES = 15L

    fun begin(context: Context) {
        val expiresAt = System.currentTimeMillis() + DURATION_MINUTES * 60_000L
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
        UpdateScheduler.scheduleAutoRelock(context, DURATION_MINUTES)
    }

    fun end(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_EXPIRES_AT)
            .apply()
        UpdateScheduler.cancelAutoRelock(context)
    }

    fun isActive(context: Context): Boolean {
        val expiresAt = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_EXPIRES_AT, 0L)
        return expiresAt > System.currentTimeMillis()
    }
}
