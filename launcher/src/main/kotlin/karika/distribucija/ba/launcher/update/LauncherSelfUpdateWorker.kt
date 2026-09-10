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
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.logging.AppLogger

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
 *
 * The installedVersionCode() check below is not just an optimization: because the process dies
 * mid-install as described above, doWork() never gets the chance to hand WorkManager a Result at
 * all for a successful update - WorkManager sees that as interrupted work, not completed work,
 * and retries this exact same unique job once the relaunched process comes back up (see
 * BootBroadcastReceiver's MY_PACKAGE_REPLACED handling). Without this check, that retry would
 * re-download and reinstall the same already-current build, kill the process again, and repeat
 * forever - which is exactly what was observed on a real device. With it, the retry's first move
 * is noticing the target version is no longer newer than what is already running, and stopping.
 */
class LauncherSelfUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppLogger.i(TAG, "doWork start")
        val latest = DashboardApi.fetchLatestVersion(DeviceIdentity.id(applicationContext), app = "launcher")
        if (!latest.isPublished) {
            Log.i(TAG, "No launcher version published yet, nothing to install")
            return Result.success()
        }

        val installedVersionCode = installedVersionCode()
        if (latest.versionCode <= installedVersionCode) {
            Log.i(TAG, "Launcher already on version code $installedVersionCode, nothing to install")
            AppLogger.i(TAG, "Already on version code $installedVersionCode, target was ${latest.versionCode} - nothing to install")
            return Result.success()
        }
        AppLogger.i(TAG, "Installing launcher ${latest.versionName} (${latest.versionCode}), currently $installedVersionCode")

        // Same reasoning as UpdateWorker's own setForeground() call - without it, this process
        // (not visibly in front while salesrep is) is eligible for the cached-app freezer mid-
        // download.
        setForeground(createForegroundInfo())

        val apkFile = ApkDownloader.download(applicationContext, latest.apkUrl) ?: run {
            AppLogger.e(TAG, "Download failed for launcher ${latest.versionName}")
            return Result.retry()
        }
        if (!ApkChecksum.verifySha256(apkFile, latest.apkSha256)) {
            Log.e(TAG, "Checksum mismatch for downloaded launcher APK (${latest.versionName}), discarding")
            AppLogger.e(TAG, "Checksum mismatch for downloaded launcher APK (${latest.versionName}), discarding")
            apkFile.delete()
            return Result.retry()
        }

        AppLogger.i(TAG, "Committing install session for launcher ${latest.versionName} - process will be killed shortly if this succeeds")
        val installed = ApkInstaller.install(applicationContext, apkFile)
        apkFile.delete()
        // If installed is true, this line logging is racing the system killing this process to
        // apply the update - it may or may not make it to disk before that happens. See
        // BootBroadcastReceiver's MY_PACKAGE_REPLACED handling for what runs after the kill.
        AppLogger.i(TAG, "ApkInstaller.install returned $installed")
        return if (installed) Result.success() else Result.retry()
    }

    private fun installedVersionCode(): Long = try {
        val info = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
    } catch (e: PackageManager.NameNotFoundException) {
        0L
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

        /** Also read by BootBroadcastReceiver, to cancel this if it's still showing once the
         * updated process comes back up - see that class's own comment. */
        const val NOTIFICATION_ID = 2
    }
}
