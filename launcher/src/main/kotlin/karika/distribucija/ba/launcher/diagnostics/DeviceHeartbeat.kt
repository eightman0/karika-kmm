package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.RemoteMaintenanceState
import karika.distribucija.ba.launcher.provision.DeviceConfig
import karika.distribucija.ba.launcher.update.DeviceApi
import kotlinx.coroutines.tasks.await
import org.json.JSONArray

/**
 * Reports what's installed, battery state, and any GPS fixes queued since the last successful
 * report, so Karika Ops' device list has something to show - without this, a device
 * only shows up once someone requests a log pull. Called from UpdateWorker on every check
 * (periodic + push-triggered), so freshness matches that cadence.
 */
object DeviceHeartbeat {
    private const val TAG = "DeviceHeartbeat"

    /** The server rejects a heartbeat carrying more than this many locations (422). */
    private const val MAX_LOCATIONS = 500

    suspend fun report(context: Context, packageName: String, versionCode: Long, versionName: String) {
        if (!DeviceConfig.isEnrolled(context)) return
        try {
            val fcmToken = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            // -1 (property unsupported/unavailable) is BatteryManager's own sentinel for "no
            // reading" - reported as null rather than a misleading 0% or -1%.
            val batteryLevel = batteryManager
                ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                ?.takeIf { it in 0..100 }
            // Only salesrep queues GPS fixes - shop devices are stationary, nothing to read there.
            val locations = if (packageName == KnownApps.SALESREP.packageName) {
                oldest(LocationHistoryReader.readNewPoints(context), MAX_LOCATIONS)
            } else {
                JSONArray()
            }
            val (launcherVersionCode, launcherVersionName) = installedVersion(context, context.packageName)
            DeviceApi.reportHeartbeat(
                context = context,
                installedPackage = packageName,
                installedVersionCode = versionCode,
                installedVersionName = versionName,
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE,
                deviceModel = Build.MODEL,
                fcmToken = fcmToken,
                // Same condition LauncherActivity.refreshMaintenanceState() uses to decide
                // whether to show the banner - covers both the admin-triggered flag and the
                // auto-expiring one UpdateWorker sets during an install.
                maintenanceActive = MaintenanceState.isActive(context) || RemoteMaintenanceState.isActive(context),
                batteryLevel = batteryLevel,
                batteryCharging = batteryManager?.isCharging,
                locations = locations,
                launcherVersionCode = launcherVersionCode,
                launcherVersionName = launcherVersionName
            )
            // Only recorded once the send above actually succeeds - if it throws, this line never
            // runs and the same points are still "new" on the next heartbeat attempt. Any backlog
            // past MAX_LOCATIONS is still newer than what's marked here, so it goes out next time.
            LocationHistoryReader.markSent(context, locations)
        } catch (e: Exception) {
            Log.w(TAG, "Heartbeat failed: ${e.message}")
        }
    }

    /** Points are in ts order, so this keeps a backlog draining oldest-first across heartbeats. */
    private fun oldest(points: JSONArray, max: Int): JSONArray {
        if (points.length() <= max) return points
        val capped = JSONArray()
        for (i in 0 until max) capped.put(points.get(i))
        return capped
    }

    /** Shared by UpdateWorker (the payload app's version) and HeartbeatAlarmReceiver (also the
     * payload app's version, via the AlarmManager-triggered fallback path) - a plain PackageInfo
     * lookup, so it works for any installed package, not just this app's own. */
    fun installedVersion(context: Context, packageName: String): Pair<Long, String> = try {
        val info = context.packageManager.getPackageInfo(packageName, 0)
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
        versionCode to info.versionName.orEmpty()
    } catch (e: PackageManager.NameNotFoundException) {
        0L to ""
    }
}
