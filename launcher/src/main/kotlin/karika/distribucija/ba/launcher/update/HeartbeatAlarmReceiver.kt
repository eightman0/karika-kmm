package karika.distribucija.ba.launcher.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.diagnostics.DeviceHeartbeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Backstop for the heartbeat, independent of WorkManager/JobScheduler - a real device was found
 * with its JobScheduler thermal-status job restriction stuck (Android's own "Restricted due to:
 * thermal." gate), which had silently blocked UpdateWorker (and therefore the heartbeat inside it)
 * from running for over an hour despite every live temperature sensor reading normal - a stale
 * cached thermal status on this OEM's build, only ever confirmed clearable via
 * `adb shell cmd thermalservice override-status`, not something an app can invoke itself.
 *
 * AlarmManager is a separate subsystem from JobScheduler and is not subject to that restriction
 * (confirmed: AlarmManagerService has no thermal-status awareness at all), so scheduling this
 * heartbeat through it keeps the dashboard's "is this device alive" signal working even during a
 * stuck-thermal episode that silently kills every WorkManager-based path (periodic heartbeat,
 * immediate push-triggered checks, self-update, all of it). Deliberately reports only - no update
 * logic here, to keep this path minimal and unlikely to be affected by whatever wider issue is
 * blocking the WorkManager side.
 */
class HeartbeatAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val targetPackage = KnownApps.PRIMARY.packageName
                val (versionCode, versionName) = DeviceHeartbeat.installedVersion(context, targetPackage)
                DeviceHeartbeat.report(context, targetPackage, versionCode, versionName)
            } finally {
                UpdateScheduler.scheduleHeartbeatAlarm(context)
                pendingResult.finish()
            }
        }
    }
}
