package karika.distribucija.ba.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * Indefinite maintenance flag toggled by an admin via the maintenance_on/maintenance_off FCM
 * commands - separate from MaintenanceState, which auto-expires and is only ever set internally
 * during an update install. This one stays on until explicitly turned back off, for cases like
 * "pull this tablet out of service until someone's on site".
 */
object RemoteMaintenanceState {
    private const val PREFS = "remote_maintenance_state"
    private const val KEY_ACTIVE = "active"

    fun begin(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ACTIVE, true)
            .apply()
    }

    fun end(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ACTIVE, false)
            .apply()
    }

    fun isActive(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVE, false)

    fun addChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(listener)
    }
}
