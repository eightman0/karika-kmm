package karika.distribucija.ba.launcher.provision

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import karika.distribucija.ba.launcher.KnownApps

/**
 * One-time (per install) prompt asking to exempt the launcher and salesrep from battery
 * optimizations. Device Owner does NOT do this automatically here - the Doze/App Standby
 * whitelist (DEVICE_POWER) is a signature|privileged permission, unreachable even to a Device
 * Owner app, confirmed by `adb shell dumpsys deviceidle whitelist` showing neither app present
 * on a real device. Without this exemption both apps are subject to normal Doze throttling and
 * can stop reporting/receiving pushes after an idle period - observed on a real device as a
 * multi-hour freeze overnight that only a manual reboot recovered from.
 *
 * Requires one tap per app, shown once during initial setup while a technician is still present
 * and provisioning the device - not a recurring nuisance. If it is ever missed/denied, the only
 * other way to fix it after the fact is `adb shell dumpsys deviceidle whitelist +<package>`
 * (see the debug_unlock command for getting ADB access on a device that would otherwise refuse to
 * show the authorization dialog).
 *
 * Has to fire before lock task ever engages, not after: like every other system dialog on this
 * fleet, this one does not draw over a pinned kiosk activity, and by the time LauncherActivity's
 * onResume() would normally reach this call, kiosk.enter() has usually already pinned it. Callers
 * (LauncherKiosk) skip re-engaging lock task for exactly the one resume where this returns true -
 * the very next resume, once the technician has dismissed the dialog(s), finds isAsked() already
 * true and locks down normally.
 */
object BatteryOptimizationPrompt {
    private const val PREFS = "battery_optimization_prompt"
    private const val KEY_ASKED = "asked"

    /** Returns true only on the one call that actually fires the prompt(s) - false every time
     * after (already asked), so lock task only ever gets skipped for that single resume. */
    fun askOnceIfNeeded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ASKED, false)) return false
        prefs.edit().putBoolean(KEY_ASKED, true).apply()

        requestExemption(context, context.packageName)
        requestExemption(context, KnownApps.PRIMARY.packageName)
        return true
    }

    private fun requestExemption(context: Context, packageName: String) {
        val powerManager = context.getSystemService(PowerManager::class.java) ?: return
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) return
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
