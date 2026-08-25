package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import karika.distribucija.ba.launcher.update.DashboardApi
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.AppLogger
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Support-log pull: the admin dashboard sends an FCM push (see KioskMessagingService) instead of
 * setting a field a Firestore listener here used to watch for - every known app's local log (this
 * app's own via AppLogger, salesrep's via its LogProvider content URI) gets zipped and uploaded to
 * Storage, then the dashboard's device API is told where to find it. Storage itself (not
 * Firestore) still needs an authenticated client per storage.rules, hence the anonymous sign-in.
 */
object LogUploadManager {
    private const val TAG = "LogUploadManager"
    private const val SALESREP_LOG_AUTHORITY = "karika.distribucija.ba.salesrep.logs"

    suspend fun uploadNow(context: Context, requestedAt: String?) {
        val appContext = context.applicationContext
        try {
            if (Firebase.auth.currentUser == null) {
                Firebase.auth.signInAnonymously().await()
            }
            val deviceId = DeviceIdentity.id(appContext)
            uploadLogs(appContext, deviceId, requestedAt)
        } catch (e: Exception) {
            Log.e(TAG, "Log upload failed", e)
        }
    }

    private suspend fun uploadLogs(context: Context, deviceId: String, requestedAt: String?) {
        val zipFile = File(context.cacheDir, "logs-upload.zip")
        try {
            ZipOutputStream(zipFile.outputStream()).use { zip ->
                addFileToZip(zip, AppLogger.currentLogFile(), "launcher.log")
                addFileToZip(zip, AppLogger.backupLogFile(), "launcher.log.1")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/current", "salesrep.log")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/backup", "salesrep.log.1")
            }

            val storagePath = "logs/$deviceId/${System.currentTimeMillis()}.zip"
            val storageRef = Firebase.storage.reference.child(storagePath)
            storageRef.putFile(Uri.fromFile(zipFile)).await()
            val downloadUrl = storageRef.downloadUrl.await()

            // downloadUrl only works for an authenticated Firebase client (subject to
            // storage.rules); the admin dashboard reads the stored path instead and mints its own
            // signed URL server-side, bypassing those rules entirely.
            DashboardApi.reportLogUploaded(deviceId, downloadUrl.toString(), storagePath, requestedAt)
            Log.i(TAG, "Log upload complete: $downloadUrl")
        } finally {
            zipFile.delete()
        }
    }

    /** Fleet-wide broadcast, not a per-device request-then-fulfill flow like uploadNow() - every
     * device that's on and connected uploads its own local analytics events independently. */
    suspend fun uploadAnalyticsNow(context: Context) {
        val appContext = context.applicationContext
        try {
            if (Firebase.auth.currentUser == null) {
                Firebase.auth.signInAnonymously().await()
            }
            val deviceId = DeviceIdentity.id(appContext)
            uploadAnalytics(appContext, deviceId)
        } catch (e: Exception) {
            Log.e(TAG, "Analytics upload failed", e)
        }
    }

    private suspend fun uploadAnalytics(context: Context, deviceId: String) {
        val zipFile = File(context.cacheDir, "analytics-upload.zip")
        try {
            ZipOutputStream(zipFile.outputStream()).use { zip ->
                addFileToZip(zip, AnalyticsTracker.currentFile(), "launcher.jsonl")
                addFileToZip(zip, AnalyticsTracker.currentBackupFile(), "launcher.jsonl.1")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/analytics_current", "salesrep.jsonl")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/analytics_backup", "salesrep.jsonl.1")
            }

            val storagePath = "analytics/$deviceId/${System.currentTimeMillis()}.zip"
            val storageRef = Firebase.storage.reference.child(storagePath)
            storageRef.putFile(Uri.fromFile(zipFile)).await()
            val downloadUrl = storageRef.downloadUrl.await()

            DashboardApi.reportAnalyticsUploaded(deviceId, downloadUrl.toString(), storagePath)
            Log.i(TAG, "Analytics upload complete: $downloadUrl")
        } finally {
            zipFile.delete()
        }
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File?, entryName: String) {
        if (file == null || !file.exists()) return
        writeEntry(zip, entryName) { out -> file.inputStream().use { it.copyTo(out) } }
    }

    private fun addUriToZip(context: Context, zip: ZipOutputStream, uri: String, entryName: String) {
        val input: InputStream = try {
            context.contentResolver.openInputStream(Uri.parse(uri)) ?: return
        } catch (e: Exception) {
            Log.w(TAG, "Could not read $uri: ${e.message}")
            return
        }
        writeEntry(zip, entryName) { out -> input.use { it.copyTo(out) } }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, write: (OutputStream) -> Unit) {
        zip.putNextEntry(ZipEntry(name))
        write(zip)
        zip.closeEntry()
    }
}
