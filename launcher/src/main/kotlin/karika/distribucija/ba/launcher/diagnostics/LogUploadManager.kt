package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import karika.distribucija.ba.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Support-log pull: an admin sets `logRequestedAt` on this device's Firestore document (today,
 * by hand in the Firebase console - there's no admin dashboard yet), a real-time listener here
 * picks it up, and every known app's local log (this app's own via AppLogger, salesrep's via its
 * LogProvider content URI) gets zipped and uploaded to Storage. No polling, same real-time
 * pattern as RemoteConfigProvider.
 */
object LogUploadManager {
    private const val TAG = "LogUploadManager"
    private const val SALESREP_LOG_AUTHORITY = "karika.distribucija.ba.salesrep.logs"

    private val scope = CoroutineScope(Dispatchers.IO)

    fun start(context: Context) {
        val appContext = context.applicationContext
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { listenForTrigger(appContext) }
            .addOnFailureListener { e -> Log.e(TAG, "Anonymous sign-in failed, log upload disabled", e) }
    }

    private fun listenForTrigger(context: Context) {
        val deviceId = DeviceIdentity.id(context)
        Firebase.firestore.collection("devices").document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listener error", error)
                    return@addSnapshotListener
                }
                val requestedAt = snapshot?.getTimestamp("logRequestedAt") ?: return@addSnapshotListener
                val handledAt = snapshot.getTimestamp("lastLogUploadRequestHandledAt")
                if (handledAt != null && handledAt >= requestedAt) return@addSnapshotListener

                scope.launch { uploadLogs(context, deviceId, requestedAt) }
            }
    }

    private suspend fun uploadLogs(context: Context, deviceId: String, requestedAt: Timestamp) {
        val zipFile = File(context.cacheDir, "logs-upload.zip")
        try {
            ZipOutputStream(zipFile.outputStream()).use { zip ->
                addFileToZip(zip, AppLogger.currentLogFile(), "launcher.log")
                addFileToZip(zip, AppLogger.backupLogFile(), "launcher.log.1")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/current", "salesrep.log")
                addUriToZip(context, zip, "content://$SALESREP_LOG_AUTHORITY/backup", "salesrep.log.1")
            }

            val storageRef = Firebase.storage.reference
                .child("logs/$deviceId/${requestedAt.seconds}.zip")
            storageRef.putFile(Uri.fromFile(zipFile)).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Firebase.firestore.collection("devices").document(deviceId)
                .update(
                    mapOf(
                        "lastLogUploadUrl" to downloadUrl.toString(),
                        "lastLogUploadAt" to Timestamp.now(),
                        "lastLogUploadRequestHandledAt" to requestedAt
                    )
                ).await()
            Log.i(TAG, "Log upload complete: $downloadUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Log upload failed", e)
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
