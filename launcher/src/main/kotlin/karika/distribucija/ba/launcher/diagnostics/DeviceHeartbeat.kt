package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.RemoteMaintenanceState
import karika.distribucija.ba.launcher.update.DashboardApi
import kotlinx.coroutines.tasks.await

/**
 * Reports what's installed, battery state, and any GPS fixes queued since the last successful
 * report, so the admin dashboard's device list has something to show - without this, a device
 * only shows up once someone requests a log pull. Called from UpdateWorker on every check
 * (periodic + push-triggered), so freshness matches that cadence.
 */
object DeviceHeartbeat {
    private const val TAG = "DeviceHeartbeat"

    suspend fun report(context: Context, packageName: String, versionCode: Long, versionName: String) {
        try {
            val deviceId = DeviceIdentity.id(context)
            val fcmToken = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            // -1 (property unsupported/unavailable) is BatteryManager's own sentinel for "no
            // reading" - reported as null rather than a misleading 0% or -1%.
            val batteryLevel = batteryManager
                ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                ?.takeIf { it in 0..100 }
            val locations = LocationHistoryReader.readNewPoints(context)
            val (launcherVersionCode, launcherVersionName) = installedVersion(context, context.packageName)
            DashboardApi.reportHeartbeat(
                deviceId = deviceId,
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
            // runs and the same points are still "new" on the next heartbeat attempt.
            LocationHistoryReader.markSent(context, locations)
        } catch (e: Exception) {
            Log.w(TAG, "Heartbeat failed: ${e.message}")
        }
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
