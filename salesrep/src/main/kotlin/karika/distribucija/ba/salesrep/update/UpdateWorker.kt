package karika.distribucija.ba.salesrep.update

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.salesrep.BuildConfig
import java.io.File
import java.security.MessageDigest

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        RemoteConfigProvider.fetchLatest()
        val latest = RemoteConfigProvider.latestVersion()

        if (!latest.isPublished || latest.versionCode <= BuildConfig.VERSION_CODE) {
            Log.i(TAG, "Already on latest version (${BuildConfig.VERSION_CODE})")
            Result.success()
        } else {
            Log.i(TAG, "New version available: ${latest.versionCode} (${latest.versionName})")
            runUpdate(latest)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Update check failed", e)
        Result.retry()
    }

    private suspend fun runUpdate(latest: KioskVersion): Result {
        val apkFile = ApkDownloader.download(applicationContext, latest.apkUrl) ?: return Result.retry()

        if (!verifyChecksum(apkFile, latest.apkSha256)) {
            Log.e(TAG, "Checksum mismatch for downloaded APK (version ${latest.versionCode}), discarding")
            apkFile.delete()
            return Result.retry()
        }

        val installed = ApkInstaller.install(applicationContext, apkFile)
        apkFile.delete()
        return if (installed) Result.success() else Result.retry()
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
