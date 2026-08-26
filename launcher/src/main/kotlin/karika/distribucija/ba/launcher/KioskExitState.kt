package karika.distribucija.ba.launcher

import android.content.Context

/**
 * Bounded window during which LauncherKiosk.enter() skips re-locking the device and
 * LauncherActivity skips auto-relaunching salesrep - without this, the instant LauncherActivity
 * is next resumed after an exit_kiosk command (which is exactly what tends to happen right after
 * salesrep unlocks and steps aside) it would immediately re-arm lock task and relaunch salesrep,
 * undoing the exit before anyone could actually use the open device.
 *
 * Expires on its own (ExitKioskFlow also schedules ReArmKioskWorker for the same deadline) rather
 * than needing an explicit "re-lock" command, so a support session nobody remembers to close
 * doesn't leave a tablet unlocked in the field indefinitely. Also explicitly cleared on every
 * fresh process start (LauncherApp.onCreate()) - it is SharedPreferences-backed, so without that
 * it would otherwise survive a reboot too, and rebooting the device is itself as strong a signal
 * as any that whoever is standing in front of it wants lockdown back immediately, not whenever
 * the original window happens to run out.
 */
object KioskExitState {
    private const val PREFS = "kiosk_exit_state"
    private const val KEY_UNTIL = "until"

    fun begin(context: Context, durationMs: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_UNTIL, System.currentTimeMillis() + durationMs)
            .apply()
    }

    fun end(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_UNTIL)
            .apply()
    }

    fun isActive(context: Context): Boolean {
        val until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_UNTIL, 0L)
        return System.currentTimeMillis() < until
    }
}
