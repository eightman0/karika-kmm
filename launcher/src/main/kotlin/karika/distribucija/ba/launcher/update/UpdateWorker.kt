package karika.distribucija.ba.launcher.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.diagnostics.DeviceHeartbeat
import java.io.File
import java.security.MessageDigest

private data class InstalledInfo(val versionCode: Long, val versionName: String)

/** Keeps KnownApps.PRIMARY (salesrep) up to date - not the launcher itself. Also handles the
 * very first install: a not-yet-installed package reads as version 0, which always compares as
 * older than any published version. */
class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val latest = VersionConfigProvider.fetchLatest()
        val targetPackage = KnownApps.PRIMARY.packageName
        val installed = installedInfo(targetPackage)

        DeviceHeartbeat.report(applicationContext, targetPackage, installed.versionCode, installed.versionName)

        if (!latest.isPublished || latest.versionCode <= installed.versionCode) {
            Log.i(TAG, "$targetPackage already on latest version (${installed.versionCode})")
            Result.success()
        } else {
            Log.i(TAG, "New version available for $targetPackage: ${latest.versionCode} (${latest.versionName})")
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
        try {
            val apkFile = ApkDownloader.download(applicationContext, latest.apkUrl) ?: return Result.retry()

            if (!verifyChecksum(apkFile, latest.apkSha256)) {
                Log.e(TAG, "Checksum mismatch for downloaded APK (version ${latest.versionCode}), discarding")
                apkFile.delete()
                return Result.retry()
            }

            val installed = ApkInstaller.install(applicationContext, apkFile)
            apkFile.delete()
            if (!installed) return Result.retry()

            // Otherwise the dashboard keeps showing the pre-update version (and a stale
            // "lagging" tag) until whatever unrelated event triggers the next heartbeat -
            // there's no guarantee that happens soon after a real-time-triggered install.
            DeviceHeartbeat.report(applicationContext, KnownApps.PRIMARY.packageName, latest.versionCode, latest.versionName)
            return Result.success()
        } finally {
            MaintenanceState.end(applicationContext)
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
    }
}
