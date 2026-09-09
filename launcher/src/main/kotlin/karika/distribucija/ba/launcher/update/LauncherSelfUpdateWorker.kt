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
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity

/**
 * Downloads and silently installs the launcher's own latest resolved version - published/staged
 * through launcher_version_config.py, a parallel track to salesrep's version_config.py (see that
 * file's schema comment for why they're separate rather than a generalized one). The only other
 * way to update this Device Owner app is a full factory reset and QR re-scan, or a developer
 * connected over USB.
 *
 * Mirrors UpdateWorker's download-verify-install shape, but there's no post-install bookkeeping
 * to do here: installing an update to this process's own package gets it killed and replaced by
 * the system, likely before anything after commit() would reliably run - success is really
 * confirmed by the device coming back up as a working launcher on its next heartbeat (which now
 * always reports the launcher's own version too, see DeviceHeartbeat), not by anything this
 * worker does afterward. The ack for the command that triggered this is sent up front too, see
 * KioskMessagingService.
 */
class LauncherSelfUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val latest = DashboardApi.fetchLatestVersion(DeviceIdentity.id(applicationContext), app = "launcher")
        if (!latest.isPublished) {
            Log.i(TAG, "No launcher version published yet, nothing to install")
            return Result.success()
        }

        // Same reasoning as UpdateWorker's own setForeground() call - without it, this process
        // (not visibly in front while salesrep is) is eligible for the cached-app freezer mid-
        // download.
        setForeground(createForegroundInfo())

        val apkFile = ApkDownloader.download(applicationContext, latest.apkUrl) ?: return Result.retry()
        if (!ApkChecksum.verifySha256(apkFile, latest.apkSha256)) {
            Log.e(TAG, "Checksum mismatch for downloaded launcher APK (${latest.versionName}), discarding")
            apkFile.delete()
            return Result.retry()
        }

        val installed = ApkInstaller.install(applicationContext, apkFile)
        apkFile.delete()
        return if (installed) Result.success() else Result.retry()
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
        private const val TAG = "LauncherSelfUpdate"
        private const val NOTIFICATION_CHANNEL_ID = "launcher_update_in_progress"
        private const val NOTIFICATION_ID = 2
    }
}
