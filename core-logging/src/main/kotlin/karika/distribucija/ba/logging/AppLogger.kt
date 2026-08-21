package karika.distribucija.ba.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Writes every log line to a rotating local file (in addition to logcat), so a device's history
 * survives past whatever's still in the kernel log ring buffer, and can be pulled for support
 * purposes later - see the launcher module's log-upload piece. Every line is tagged with
 * whatever user is currently set via [setUser], since that's what a support request is actually
 * keyed on, not a raw PID/timestamp the way logcat is.
 */
object AppLogger {
    private const val LOG_DIR_NAME = "logs"
    private const val LOG_FILE_NAME = "app.log"
    private const val BACKUP_FILE_NAME = "app.log.1"
    private const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024

    private var logFile: File? = null
    private var backupFile: File? = null

    @Volatile
    private var userId: String? = null

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    /** Call once, early in Application.onCreate(). */
    fun init(context: Context) {
        val dir = File(context.getExternalFilesDir(null), LOG_DIR_NAME).apply { mkdirs() }
        logFile = File(dir, LOG_FILE_NAME)
        backupFile = File(dir, BACKUP_FILE_NAME)
    }

    fun setUser(id: String?) {
        userId = id
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        appendLine("D", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        appendLine("I", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        appendLine("W", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        appendLine("E", tag, message)
        if (throwable != null) {
            appendRaw(Log.getStackTraceString(throwable))
        }
    }

    /** The files a ContentProvider bridge (see salesrep's LogProvider) serves to the launcher. */
    fun currentLogFile(): File? = logFile

    fun backupLogFile(): File? = backupFile

    private fun appendLine(level: String, tag: String, message: String) {
        val user = userId ?: "-"
        appendRaw("${dateFormat.format(Date())} $level/$tag user=$user: $message")
    }

    @Synchronized
    private fun appendRaw(line: String) {
        val file = logFile ?: return
        rotateIfNeeded(file)
        runCatching { file.appendText(line + "\n") }
    }

    private fun rotateIfNeeded(file: File) {
        val backup = backupFile ?: return
        if (file.exists() && file.length() > MAX_FILE_SIZE_BYTES) {
            runCatching {
                backup.delete()
                file.renameTo(backup)
            }
        }
    }
}
