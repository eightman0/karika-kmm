package karika.distribucija.ba.launcher.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.diagnostics.DeviceHeartbeat
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import java.io.File
import java.security.MessageDigest

private data class InstalledInfo(val versionCode: Long, val versionName: String)

/** Keeps KnownApps.PRIMARY (salesrep) up to date - not the launcher itself. Whether to install is
 * decided by comparing the published APK's sha256 against the one this device last installed,
 * not by version numbers - a publish only has to contain a new APK, nothing has to be typed or
 * incremented correctly for the update to actually reach devices. The heartbeat below still
 * reports whatever version is genuinely installed, whatever that happens to be. */
class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val latest = DashboardApi.fetchLatestVersion(DeviceIdentity.id(applicationContext))
        val targetPackage = KnownApps.PRIMARY.packageName
        val installed = installedInfo(targetPackage)

        DeviceHeartbeat.report(applicationContext, targetPackage, installed.versionCode, installed.versionName)

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

    private fun installedInfo(packageName: String): InstalledInfo = try {
        val info = applicationContext.packageManager.getPackageInfo(packageName, 0)
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
        InstalledInfo(versionCode, info.versionName.orEmpty())
    } catch (e: PackageManager.NameNotFoundException) {
        InstalledInfo(0L, "")
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

            if (!verifyChecksum(apkFile, latest.apkSha256)) {
                Log.e(TAG, "Checksum mismatch for downloaded APK (${latest.versionName}), discarding")
                apkFile.delete()
                return Result.retry()
            }

            val installed = ApkInstaller.install(applicationContext, apkFile)
            apkFile.delete()
            if (!installed) return Result.retry()

            InstalledApkState.setLastInstalledSha256(applicationContext, latest.apkSha256)
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

    private fun verifyChecksum(file: File, expectedSha256: String): Boolean {
        if (expectedSha256.isBlank()) {
            Log.w(TAG, "No apkSha256 published for this version, skipping integrity check")
            return true
        }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DIGEST_BUFFER_SIZE)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        return actual.equals(expectedSha256, ignoreCase = true)
    }

    companion object {
        private const val TAG = "UpdateWorker"
        private const val DIGEST_BUFFER_SIZE = 8192
        private const val NOTIFICATION_CHANNEL_ID = "update_in_progress"
        private const val NOTIFICATION_ID = 1
    }
}
