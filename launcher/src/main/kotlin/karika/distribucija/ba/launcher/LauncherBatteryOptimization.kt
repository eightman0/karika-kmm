package karika.distribucija.ba.launcher

import android.content.Context

/**
 * Tracks whether the launcher's own one-time battery-optimization exemption request (see
 * LauncherActivity) has been resolved yet - not just asked, but actually returned from (Allow or
 * Deny, either way) or found unnecessary (already exempt). UpdateWorker checks this before
 * installing salesrep for the first time, so that install does not race the technician still
 * looking at the system dialog: a real device was found with neither package on the Doze/battery-
 * optimization whitelist despite the launcher being Device Owner (AOSP's "Device Owner apps are
 * automatically exempt" claim does not hold on this OEM build), which this dialog is the only
 * in-app way to fix for the launcher itself.
 */
object LauncherBatteryOptimization {
    private const val PREFS = "battery_optimization_prompt"
    private const val KEY_RESOLVED = "resolved"

    fun isResolved(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_RESOLVED, false)

    fun markResolved(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_RESOLVED, true)
            .apply()
    }
}
