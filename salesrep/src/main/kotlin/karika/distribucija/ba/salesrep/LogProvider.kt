package karika.distribucija.ba.salesrep

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import karika.distribucija.ba.logging.AppLogger
import java.io.File
import java.io.FileNotFoundException

/**
 * Read-only bridge that lets the launcher pull this app's local log files for support requests.
 * Guarded by a `normal`-level custom permission (not `signature`) since a future payload app
 * added to the launcher's KnownApps list may not share its signing key - see AndroidManifest.xml.
 */
class LogProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val file = fileFor(uri) ?: throw FileNotFoundException("Unknown log uri: $uri")
        if (!file.exists()) throw FileNotFoundException("No log file yet: ${file.name}")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val file = fileFor(uri) ?: return null
        return MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply {
            addRow(arrayOf(file.name, file.length()))
        }
    }

    override fun getType(uri: Uri): String = "text/plain"

    override fun insert(uri: Uri, values: ContentValues?): Uri =
        throw UnsupportedOperationException("LogProvider is read-only")

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("LogProvider is read-only")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("LogProvider is read-only")

    private fun fileFor(uri: Uri): File? = when (uri.lastPathSegment) {
        "current" -> AppLogger.currentLogFile()
        "backup" -> AppLogger.backupLogFile()
        else -> null
    }
}
