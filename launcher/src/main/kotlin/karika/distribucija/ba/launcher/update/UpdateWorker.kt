package karika.distribucija.ba.launcher.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.LauncherBatteryOptimization
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.diagnostics.DeviceHeartbeat
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity

/** Keeps KnownApps.PRIMARY (salesrep) up to date - not the launcher itself. Whether to install is
 * decided by comparing the published APK's sha256 against the one this device last installed,
 * not by version numbers - a publish only has to contain a new APK, nothing has to be typed or
 * incremented correctly for the update to actually reach devices. The heartbeat below still
 * reports whatever version is genuinely installed, whatever that happens to be. */
class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        // Waits for the launcher's own one-time battery-optimization dialog (see LauncherActivity)
        // to be resolved before ever installing salesrep for the first time - otherwise this could
        // start (and MaintenanceState.begin()) while that dialog is still open, racing the same
        // maintenance-state flag it uses. WorkManager's linear backoff retries this within seconds,
        // not minutes, so a technician still looking at the dialog barely notices the delay.
        if (!LauncherBatteryOptimization.isResolved(applicationContext)) {
            return Result.retry()
        }
        val latest = DashboardApi.fetchLatestVersion(DeviceIdentity.id(applicationContext), app = "salesrep")
        val targetPackage = KnownApps.PRIMARY.packageName
        val (installedVersionCode, installedVersionName) = DeviceHeartbeat.installedVersion(applicationContext, targetPackage)

        DeviceHeartbeat.report(applicationContext, targetPackage, installedVersionCode, installedVersionName)

        val alreadyInstalled = latest.apkSha256.isNotBlank() &&
            latest.apkSha256.equals(InstalledApkState.lastInstalledSha256(applicationContext), ignoreCase = true)
        if (!latest.isPublished || alreadyInstalled) {
            Log.i(TAG, "$targetPackage already on the published build")
            Result.success()
        } else {
            Log.i(TAG, "New build published for $targetPackage (${latest.versionName}), installing")
            runUpdate(latest)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Update check failed", e)
        MaintenanceState.end(applicationContext)
        Result.retry()
    }

    private suspend fun runUpdate(latest: KioskVersion): Result {
        MaintenanceState.begin(applicationContext)
        // Without this, a backgrounded launcher process (salesrep in front, nothing visible of
        // ours) is eligible for the OS's cached-app freezer - it can get frozen mid-download with
        // no CPU time at all, hanging here indefinitely until something else kills the process
        // (observed: stuck for 8+ minutes until a reinstall tore it down). A foreground service
        // is explicitly exempt from that freeze.
        setForeground(createForegroundInfo())
        try {
            val apkFile = ApkDownloader.download(applicationContext, latest.apkUrl) ?: return Result.retry()

            if (!ApkChecksum.verifySha256(apkFile, latest.apkSha256)) {
                Log.e(TAG, "Checksum mismatch for downloaded APK (${latest.versionName}), discarding")
                apkFile.delete()
                return Result.retry()
            }

            val installed = ApkInstaller.install(applicationContext, apkFile)
            apkFile.delete()
            if (!installed) return Result.retry()

            InstalledApkState.setLastInstalledSha256(applicationContext, latest.apkSha256)
            // Cleared before reporting, not left to the finally block below - otherwise this
            // heartbeat (sent to promptly reflect the new version, see comment below) would still
            // read MaintenanceState as active and report a device that just finished updating as
            // stuck "in maintenance". The finally block's end() becomes a harmless no-op here.
            MaintenanceState.end(applicationContext)
            // Otherwise the dashboard keeps showing the pre-update version (and a stale
            // "lagging" tag) until whatever unrelated event triggers the next heartbeat -
            // there's no guarantee that happens soon after a real-time-triggered install.
            DeviceHeartbeat.report(applicationContext, KnownApps.PRIMARY.packageName, latest.versionCode, latest.versionName)
            return Result.success()
        } finally {
            MaintenanceState.end(applicationContext)
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Ažuriranje", NotificationManager.IMPORTANCE_MIN
            )
            applicationContext.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(karika.distribucija.ba.launcher.R.string.app_name))
            .setSmallIcon(karika.distribucija.ba.launcher.R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val TAG = "UpdateWorker"
        private const val NOTIFICATION_CHANNEL_ID = "update_in_progress"
        private const val NOTIFICATION_ID = 1
    }
}
