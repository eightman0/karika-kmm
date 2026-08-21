package karika.distribucija.ba.launcher.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.delay
import java.io.File

object ApkDownloader {
    private const val TAG = "ApkDownloader"
    private const val FILE_NAME = "payload-update.apk"
    private const val POLL_INTERVAL_MS = 1000L
    private const val MAX_WAIT_MS = 15 * 60 * 1000L

    /** Downloads to app-specific external storage (no runtime permission needed) and polls
     * DownloadManager until it settles. Returns null on failure or timeout - callers should
     * treat that as a retryable condition, not a fatal one. */
    suspend fun download(context: Context, apkUrl: String): File? {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val destinationFile = File(context.getExternalFilesDir(null), FILE_NAME)
        destinationFile.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Payload app update")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            setDestinationUri(Uri.fromFile(destinationFile))
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val downloadId = downloadManager.enqueue(request)

        var waited = 0L
        while (waited < MAX_WAIT_MS) {
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                if (!cursor.moveToFirst()) return null
                when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> return destinationFile
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        Log.w(TAG, "Download failed, reason=$reason")
                        return null
                    }
                }
            }
            delay(POLL_INTERVAL_MS)
            waited += POLL_INTERVAL_MS
        }
        Log.w(TAG, "Download timed out after ${MAX_WAIT_MS}ms")
        downloadManager.remove(downloadId)
        return null
    }
}
