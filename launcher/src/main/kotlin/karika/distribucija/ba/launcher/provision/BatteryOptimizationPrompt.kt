package karika.distribucija.ba.launcher.provision

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.update.UpdateScheduler

/**
 * One-time (per install) prompt asking to exempt salesrep - not this app - from battery
 * optimizations. Device Owner does NOT grant this automatically - the Doze/App Standby whitelist
 * (DEVICE_POWER) is a signature|privileged permission, unreachable even to a Device Owner app
 * (confirmed against AOSP source, including the provisioning flow itself: no PROVISIONING_* extra
 * touches it, and ManagedProvisioning does not grant it to the DPC either). The only in-app lever
 * is this interactive dialog - real MDM vendors (e.g. Hexnode) rely on the exact same one, there
 * is no silent alternative available to a normal Device Owner app.
 *
 * Only salesrep, deliberately - this app has its own AlarmManager-based heartbeat backstop
 * (UpdateScheduler.scheduleHeartbeatAlarm/HeartbeatAlarmReceiver) that works precisely because
 * setExactAndAllowWhileIdle bypasses Doze deferral on its own, without needing whitelist
 * membership - so it does not need this exemption the way salesrep, which has no such backstop,
 * does. A second dialog for this app too was tried and dropped: shown back-to-back with
 * salesrep's, they landed stacked on top of each other on a real device and the technician only
 * ever noticed and dismissed whichever one was on top.
 *
 * Requires one tap, shown once during initial setup while a technician is still present - not a
 * recurring nuisance. If it is ever missed/denied, the only way to fix it after the fact is
 * `adb shell dumpsys deviceidle whitelist +karika.distribucija.ba.salesrep` (see the debug_unlock
 * command for getting ADB access on a device that would otherwise refuse to show the
 * authorization dialog).
 *
 * Has to fire before lock task ever engages, not after: like every other system dialog on this
 * fleet, this one does not draw over a pinned kiosk activity, and by the time LauncherActivity's
 * onResume() would normally reach this call, kiosk.enter() has usually already pinned it. Callers
 * (LauncherKiosk) skip re-engaging lock task while this returns true.
 *
 * That skip has to last a real GRACE_PERIOD_MILLIS window, not just the one resume that fired the
 * prompt - confirmed on a real device: a second resume (e.g. a boot-completed relaunch landing
 * moments after the first) re-pinned lock task before the technician had a chance to see, let
 * alone tap, the dialog, which then sat open but invisible until torn down unseen. The grace
 * period keeps lock task off across however many resumes happen in that window, not just the
 * triggering one.
 */
object BatteryOptimizationPrompt {
    private const val PREFS = "battery_optimization_prompt"
    private const val KEY_ASKED_AT = "asked_at_millis"
    private const val GRACE_PERIOD_MILLIS = 60_000L

    /** True while the technician still has a window to interact with the dialog - either because
     * this call is the one that just fired it, or because a previous call fired it less than
     * GRACE_PERIOD_MILLIS ago. False once that window has passed, so lock task always eventually
     * re-engages even if the prompt was missed or ignored. */
    fun askOnceIfNeeded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val askedAt = prefs.getLong(KEY_ASKED_AT, 0L)
        if (askedAt == 0L) {
            prefs.edit().putLong(KEY_ASKED_AT, System.currentTimeMillis()).apply()
            requestExemption(context, KnownApps.PRIMARY.packageName)
            // Belt-and-suspenders on top of LauncherKiosk's own onResume()-driven check: forces a
            // re-evaluation once the grace period is up even if nothing else happens to resume
            // the launcher in the meantime (reuses the same forced-relock mechanism
            // CMD_DEBUG_UNLOCK already relies on for the same reason).
            UpdateScheduler.scheduleAutoRelock(context, GRACE_PERIOD_MILLIS / 60_000L)
            return true
        }
        return System.currentTimeMillis() - askedAt < GRACE_PERIOD_MILLIS
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
