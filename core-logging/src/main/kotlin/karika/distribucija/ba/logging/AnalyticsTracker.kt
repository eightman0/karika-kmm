package karika.distribucija.ba.logging

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Writes one JSON line per tracked screen view / button click to a rotating local file, the same
 * way AppLogger writes support logs - pulled and uploaded on demand (see the launcher's log/
 * analytics-upload piece) rather than streamed live, since usage analytics for this fleet does not
 * need to be real-time.
 */
object AnalyticsTracker {
    private const val DIR_NAME = "analytics"
    private const val FILE_NAME = "events.jsonl"
    private const val BACKUP_FILE_NAME = "events.jsonl.1"
    private const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024

    private var eventsFile: File? = null
    private var eventsBackupFile: File? = null

    @Volatile
    private var userId: String? = null

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    /** Call once, early in Application.onCreate(). */
    fun init(context: Context) {
        val dir = File(context.getExternalFilesDir(null), DIR_NAME).apply { mkdirs() }
        eventsFile = File(dir, FILE_NAME)
        eventsBackupFile = File(dir, BACKUP_FILE_NAME)
    }

    fun setUser(id: String?) {
        userId = id
    }

    fun trackScreen(screen: String) = write("screen", screen, null)

    fun trackClick(screen: String, element: String) = write("click", screen, element)

    /** The files a ContentProvider bridge (see salesrep's LogProvider) serves to the launcher. */
    fun currentFile(): File? = eventsFile

    fun currentBackupFile(): File? = eventsBackupFile

    private fun write(type: String, screen: String, element: String?) {
        val json = JSONObject()
            .put("ts", dateFormat.format(Date()))
            .put("user", userId ?: "-")
            .put("type", type)
            .put("screen", screen)
        if (element != null) json.put("element", element)
        appendRaw(json.toString())
    }

    @Synchronized
    private fun appendRaw(line: String) {
        val file = eventsFile ?: return
        rotateIfNeeded(file)
        runCatching { file.appendText(line + "\n") }
    }

    private fun rotateIfNeeded(file: File) {
        val backup = eventsBackupFile ?: return
        if (file.exists() && file.length() > MAX_FILE_SIZE_BYTES) {
            runCatching {
                backup.delete()
                file.renameTo(backup)
            }
        }
    }
}
