package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.net.Uri
import android.util.Log
import karika.distribucija.ba.launcher.update.DeviceApi
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.AppLogger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Support-log and analytics pulls, both requested over FCM (see KioskMessagingService): every
 * known app's local files (this app's own via AppLogger/AnalyticsTracker, salesrep's via its
 * LogProvider content URI) get zipped and sent as a multipart upload straight to the device API.
 * Both throw on failure, so the command's ack can report the error.
 */
object LogUploadManager {
    private const val TAG = "LogUploadManager"
    private const val SALESREP_LOG_AUTHORITY = "karika.distribucija.ba.salesrep.logs"

    suspend fun uploadLogs(context: Context, requestId: String?, requestedAt: String?) {
        val appContext = context.applicationContext
        val zipFile = File(appContext.cacheDir, "logs-upload.zip")
        try {
            writeZip(zipFile) { zip ->
                addFileToZip(zip, AppLogger.currentLogFile(), "launcher.log") or
                    addFileToZip(zip, AppLogger.backupLogFile(), "launcher.log.1") or
                    addUriToZip(appContext, zip, "content://$SALESREP_LOG_AUTHORITY/current", "salesrep.log") or
                    addUriToZip(appContext, zip, "content://$SALESREP_LOG_AUTHORITY/backup", "salesrep.log.1")
            }
            DeviceApi.uploadLogs(appContext, zipFile, requestId, requestedAt)
            Log.i(TAG, "Log upload complete (${zipFile.length()} bytes)")
        } finally {
            zipFile.delete()
        }
    }

    /** Fleet-wide broadcast, not a per-device request-then-fulfill flow like uploadLogs() - every
     * device that's on and connected uploads its own local analytics events independently. Entry
     * names are the four the server's analytics ingest reads. */
    suspend fun uploadAnalytics(context: Context, requestId: String?) {
        val appContext = context.applicationContext
        val zipFile = File(appContext.cacheDir, "analytics-upload.zip")
        try {
            writeZip(zipFile) { zip ->
                addFileToZip(zip, AnalyticsTracker.currentFile(), "launcher.jsonl") or
                    addFileToZip(zip, AnalyticsTracker.currentBackupFile(), "launcher.jsonl.1") or
                    addUriToZip(appContext, zip, "content://$SALESREP_LOG_AUTHORITY/analytics_current", "salesrep.jsonl") or
                    addUriToZip(appContext, zip, "content://$SALESREP_LOG_AUTHORITY/analytics_backup", "salesrep.jsonl.1")
            }
            DeviceApi.uploadAnalytics(appContext, zipFile, requestId)
            Log.i(TAG, "Analytics upload complete (${zipFile.length()} bytes)")
        } finally {
            zipFile.delete()
        }
    }

    /** [addEntries] returns whether it wrote anything - a zip with no entries can't be closed
     * (ZipOutputStream throws), and the server would reject it as invalid_zip anyway. */
    private fun writeZip(zipFile: File, addEntries: (ZipOutputStream) -> Boolean) {
        val zip = ZipOutputStream(zipFile.outputStream())
        val wroteAny = try {
            addEntries(zip)
        } catch (e: Exception) {
            runCatching { zip.close() }
            throw e
        }
        if (!wroteAny) {
            runCatching { zip.close() }
            throw IOException("Nothing to upload - none of the files exist on this device")
        }
        zip.close()
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File?, entryName: String): Boolean {
        if (file == null || !file.exists()) return false
        writeEntry(zip, entryName) { out -> file.inputStream().use { it.copyTo(out) } }
        return true
    }

    private fun addUriToZip(context: Context, zip: ZipOutputStream, uri: String, entryName: String): Boolean {
        val input: InputStream = try {
            context.contentResolver.openInputStream(Uri.parse(uri)) ?: return false
        } catch (e: Exception) {
            Log.w(TAG, "Could not read $uri: ${e.message}")
            return false
        }
        writeEntry(zip, entryName) { out -> input.use { it.copyTo(out) } }
        return true
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, write: (OutputStream) -> Unit) {
        zip.putNextEntry(ZipEntry(name))
        write(zip)
        zip.closeEntry()
    }
}
